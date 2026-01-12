package edu.gmu.cs321;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/****************************************************************************************************************************************************/
/***--------------------------------------------------------------- IMPORTANT NOTE ---------------------------------------------------------------***/
/***   You need servlets to connect to the db but this class is a template/example as well as a test run for when I was setting up the server.    ***/
/***-------------------------------------------  DO NOT USE THIS SERVLET FOR YOUR DATABASE OPERATIONS  -------------------------------------------***/
/***   Step 1) Create your own servlet(or duplicate this one) then name it according to your workflow step/role and add it to the web.xml file.   ***/
/***                    (Tip: You can look at how this servlet was added to the web.xml file and just copy the exact process)                     ***/
/***         Step 2) Edit your script(s) in your html file so the retrieved data is formatted properly before sending it to your servlet.         ***/
/***             Step 3) Code the methods in your servlet class so it can read the format of the transformed data you sent from html.             ***/
/***      Step 4) Finish coding all the methods tailored to your specific workflow step/role. The Object Classes should come in handy here.       ***/
/***                Step 5) Now you just gotta test it all, to make sure the code works. If it does, make the test classes for it.                ***/
/***         NOTE: I am currently on step 3, so I can kinda help up to that step but I won't be of much help on the remaining the steps.          ***/
/***     Also, w3schools & geeksforgeeks are helpful websites in general for webapp related stuff but these links specifically helped me out.     ***/
/***    Html/Javascript related Links: https://www.w3schools.com/html/html_forms_attributes.asp, https://www.w3schools.com/js/js_api_fetch.asp    ***/
/***                            Java Links: https://www.geeksforgeeks.org/java/how-to-use-preparedstatement-in-java/#                             ***/
/****************************************************************************************************************************************************/


public class ApprovalServlet extends HttpServlet {

    private static final String APPROVAL_TABLE = "Approval_Forms";

    /** doGet's purpose is to only uses the connection to pull info from the database. 
     * request: this variable is essentially what the "client" wants from this code. client: us whenever we interact with the db 
     *      example: client is requesting the first name of a Person object in the database -- this is the request
     *      The request formatting depends on your html script(s) code: 1) how did you format the html data(usually either String or JSON) 
     *                                                                  2) how did you send your formatted data to your servlet  
     * response: this variable is used to repond back to the client/website. if we continue the example, response should return the first name.
     *          note: while a response is not always necessary for every method(like delete), try to create one anyway because its good for testing.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        String formIdParam = request.getParameter("formId");
        if (formIdParam == null || formIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\":\"Missing formId parameter\"}");
            return;
        }

        int formId;
        try {
            formId = Integer.parseInt(formIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\":\"formId must be an integer\"}");
            return;
        }

        try (Connection conn = App.getConnection()) {
            if (!isInStage(conn, APPROVAL_TABLE, formId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.println("{\"error\":\"Form not found in approval\"}");
                return;
            }

            String json = loadFormJson(conn, formId);
            response.setStatus(HttpServletResponse.SC_OK);
            out.println(json);
        } catch (SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Database error\"}");
            e.printStackTrace();
        }
        
    }

    /* doPost is meant for creating a new object BUT it can also be used to update the fields of objects*/
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        
        try {
            /* ---------- reads data from approval.html ---------- */
            
            // textbox input
            String notes = req.getParameter("notes");       
            
            // "returnReview" OR "approve"
            String decision = req.getParameter("decision"); 
            
            // read formId hidden field
            String formIdStr = req.getParameter("formId");
            
            // Validate input parameters
            if (decision == null || decision.isEmpty() || formIdStr == null || formIdStr.isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"Missing required parameters: decision and formId\"}");
                return;
            }
            
            int formId = Integer.parseInt(formIdStr);

            try (Connection conn = App.getConnection()) {
                conn.setAutoCommit(false);

                if (!isInStage(conn, APPROVAL_TABLE, formId)) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"error\":\"Form not found in approval\"}");
                    return;
                }

                if ("approve".equalsIgnoreCase(decision)) {
                    approveForm(conn, formId);
                } else if ("returnReview".equalsIgnoreCase(decision)) {
                    if (notes == null || notes.isBlank()) {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        out.println("{\"error\":\"notes is required when returning\"}");
                        return;
                    }
                    returnToReview(conn, formId, notes);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.println("{\"error\":\"Invalid decision. Must be 'approve' or 'returnReview'\"}");
                    return;
                }

                conn.commit();
                resp.setStatus(HttpServletResponse.SC_OK);
                String message = "approve".equalsIgnoreCase(decision)
                        ? "Form approved and marked complete."
                        : "Form returned to review.";
                out.println("{\"success\":true,\"message\":\"" + message + "\",\"formId\":"
                        + formId + ",\"decision\":\"" + decision + "\"}");
            }

        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.println("{\"error\":\"Invalid formId format. Must be an integer.\"}");
            e.printStackTrace();
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"Database error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"" + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    /* doDelete is meant for deleting objects or fields/parameters */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

        

    }

    private boolean isInStage(Connection conn, String table, int formId) throws SQLException {
        String sql = "SELECT 1 FROM " + table + " WHERE Form_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, formId);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private String loadFormJson(Connection conn, int formId) throws SQLException {
        String sql =
                "SELECT f.Form_id, f.Imm_id, f.req_status, f.return_reason, " +
                "       p.first_name, p.last_name, p.dob, i.cur_status " +
                "FROM Forms f " +
                "JOIN Immigrants i ON f.Imm_id = i.id " +
                "JOIN Persons p ON p.id = i.id " +
                "WHERE f.Form_id = ?";

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, formId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) {
                    return "{\"error\":\"Form not found\"}";
                }
                return "{"
                        + "\"formId\":" + rs.getInt("Form_id") + ","
                        + "\"immId\":" + rs.getInt("Imm_id") + ","
                        + "\"firstName\":\"" + escapeJson(rs.getString("first_name")) + "\","
                        + "\"lastName\":\"" + escapeJson(rs.getString("last_name")) + "\","
                        + "\"dob\":\"" + escapeJson(rs.getString("dob")) + "\","
                        + "\"currentStatus\":\"" + escapeJson(rs.getString("cur_status")) + "\","
                        + "\"requestedStatus\":\"" + escapeJson(rs.getString("req_status")) + "\","
                        + "\"returnReason\":\"" + escapeJson(rs.getString("return_reason")) + "\""
                        + "}";
            }
        }
    }

    private void approveForm(Connection conn, int formId) throws SQLException {
        String immSql = "SELECT Imm_id, req_status FROM Forms WHERE Form_id = ?";
        int immId = 0;
        String reqStatus = null;
        try (PreparedStatement stmt = conn.prepareStatement(immSql)) {
            stmt.setInt(1, formId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    immId = rs.getInt("Imm_id");
                    reqStatus = rs.getString("req_status");
                }
            }
        }

        if (immId == 0 || reqStatus == null) {
            throw new SQLException("Form not found for approval");
        }

        String updateImm = "UPDATE Immigrants SET cur_status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(updateImm)) {
            stmt.setString(1, reqStatus);
            stmt.setInt(2, immId);
            stmt.executeUpdate();
        }

        clearReturnReason(conn, formId);
        moveForm(conn, APPROVAL_TABLE, "Completed_Forms", formId);
    }

    private void returnToReview(Connection conn, int formId, String reason) throws SQLException {
        updateReturnReason(conn, formId, reason);
        moveForm(conn, APPROVAL_TABLE, "Review_Forms", formId);
    }

    private void updateReturnReason(Connection conn, int formId, String reason) throws SQLException {
        String sql = "UPDATE Forms SET return_reason = ? WHERE Form_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reason);
            stmt.setInt(2, formId);
            stmt.executeUpdate();
        }
    }

    private void clearReturnReason(Connection conn, int formId) throws SQLException {
        String sql = "UPDATE Forms SET return_reason = NULL WHERE Form_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, formId);
            stmt.executeUpdate();
        }
    }

    private void moveForm(Connection conn, String fromTable, String toTable, int formId) throws SQLException {
        String deleteSql = "DELETE FROM " + fromTable + " WHERE Form_id = ?";
        String insertSql = "INSERT INTO " + toTable + " (Form_id) VALUES (?)";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, formId);
            deleteStmt.executeUpdate();
        }
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            insertStmt.setInt(1, formId);
            insertStmt.executeUpdate();
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}
