package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Approval {

    // vars should be set to null or true
    private Form form;
    private boolean noErrors;

    //used for workflow methods
    private static final int GROUP_ID = 22;

    // almost ALWAYS use this
    public Approval(){
                
        this.form = null;
        this.noErrors = true;
    }

    // should RARELY use this
    public Approval(Form form, boolean errorType){
        
        this.form = form;
        this.noErrors = !errorType;
    }



    /*** Add in getters for all parameters in second Approval Constructor and a setter ONLY for noErrors ***/
    
    public Form getForm() {
        return form;
    }

    public boolean getNoErrors() {
        return noErrors;
    }

    public void setNoErrors(boolean noErrors) {
        this.noErrors = noErrors;
    }




    // In WFUtil.java : getformfromWF (String nextstep, Integer groupid)
    
    /** gets a form object from the workflow and sets this.form equal to it. 
     * 
     * @return true if successfully retrieved from workflow ; false if failed
     */
    public boolean getFromWF() {
        // get next form id from workflow
        Integer formId = WFUtil.getformfromWF("Approve", GROUP_ID);

        // WF returns negative or zero if no work or there's an error
        if (formId == null || formId <= 0) {
            System.out.println("Approval.getFromWF: no form from workflow (id=" + formId + ")");
            return false;
        }

        App app = new App();

        try (Connection conn = app.getConnection()) {

            // Query Forms table for that form_id
            String formSql = "SELECT Imm_id, req_status FROM Forms WHERE Form_id = ?";
            int immId;
            String reqStatus;

            try (PreparedStatement stmt = conn.prepareStatement(formSql)) {
                stmt.setInt(1, formId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next()) {
                        System.out.println("Approval.getFromWF: no Forms row for Form_id=" + formId);
                        return false;
                    }
                    immId = rs.getInt("Imm_id");
                    reqStatus = rs.getString("req_status");
                }
            }

            // Query Immigrants table for current status
            String curStatus = null;
            String immSql = "SELECT cur_status FROM Immigrants WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(immSql)) {
                stmt.setInt(1, immId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        curStatus = rs.getString("cur_status");
                    }
                }
            }

            // build Form object 
            Form f = new Form(reqStatus, immId, conn);
            f.setId(formId);

            if (curStatus != null) {
                // curStatus retrieved but not stored in Form object
                // Use getCurStatusFromDB(immId) later if needed
            }

            // save on this Approval object
            this.form = f;

            System.out.println("Approval.getFromWF: loaded Form_id=" + formId +
                               " Imm_id=" + immId +
                               " curStatus=" + curStatus +
                               " reqStatus=" + reqStatus);

            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /* adds form back into review database and notifies reviewer. 
     * returns true if successfully added and notified reviewer. 
     * returns false if either failed;
     */
    public boolean informReviewer(){

        if (form == null) {
            System.out.println("Approval.informReviewer: form is null");
            return false;
        }

        App app = new App();
        boolean db = false;
        boolean wfl = false;

        // look over connection method
        try (Connection conn = app.getConnection()) {
            conn.setAutoCommit(false);

            int formId = form.getId();

            // insert into Returned_Review_Forms table in the DB
            String sql = "INSERT INTO Returned_Review_Forms (Form_id) VALUES (?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, formId);
                stmt.executeUpdate();
            }

            conn.commit();
            db = true;

        } catch (SQLException e) {
            e.printStackTrace();
            db = false;
        }

        // update workflow w/ send form back to "Review"
        Integer wfResult = WFUtil.addformtoWF(form.getId(), "Review", GROUP_ID);
        wfl = (wfResult != null && wfResult > 0);

        return db && wfl;
    }

    /* adds form into immigration database and notifies immigration office but only if errorType == "" and isValid == true
     * returns true if successfully added and notified immigration office. returns false if either failed;
     */
    public boolean informImmigration(){
        
        if (form == null) {
            System.out.println("Approval.informImmigration: form is null");
            return false;
        }

        // can only approve if form has no errors
        if (!noErrors) {
            System.out.println("Approval.informImmigration: Form has errors, cannot approve.");
            return false;
        }

        App app = new App();
        boolean db = false;
        boolean wfl = false;

        try (Connection conn = app.getConnection()) {
            conn.setAutoCommit(false);

            int formId = form.getId();
            int immId = form.getImmId();
            String newStatus = form.getReqStatus();

            // update immigrants current status
            String updateImmSql = "UPDATE Immigrants SET cur_status = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateImmSql)) {
                stmt.setString(1, newStatus);
                stmt.setInt(2, immId);
                stmt.executeUpdate();
            }

            // insert into Completed_Forms table in the DB
            String completedSql = "INSERT INTO Completed_Forms (Form_id) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(completedSql)) {
                stmt.setInt(1, formId);
                stmt.executeUpdate();
            }

            conn.commit();
            db = true;

        } catch (SQLException e) {
            e.printStackTrace();
            db = false;
        }

        // updating workflow as complete 
        Integer wfResult = WFUtil.addformtoWF(form.getId(), "Complete", GROUP_ID);
        wfl = (wfResult != null && wfResult > 0);

        return db && wfl;
    }




    /** adds form to workflow. 
     * if form has no errors uses informImmigration().
     * if form has errors, uses informReviewer().
     * @return true if successfully added ; false if it failed
     */ 
     public boolean addToWF(){
        if (form == null) {
            return false;
        }

        if (noErrors) {
            // approved and sent to imm
            return informImmigration();
        } else {
            // errors in form, sends to review
            return informReviewer();
        }
    }

    /**
     * Gets the current status from the database using immigrant ID
     * @param immigrantId the ID of the immigrant
     * @return the current status string, or null if not found
     */
    public String getCurStatusFromDB(int immigrantId) {
        App app = new App();
        String curStatus = null;

        try (Connection conn = app.getConnection()) {
            String sql = "SELECT cur_status FROM Immigrants WHERE id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, immigrantId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        curStatus = rs.getString("cur_status");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return curStatus;
    }

}
