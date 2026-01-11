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
        
        try {
            Approval approval = new Approval();
            
            // Retrieve the next form from the workflow queue
            if (approval.getFromWF()) {
                Form form = approval.getForm();
                
                if (form != null) {
                    // Get current status using the Approval helper method
                    String curStatus = approval.getCurStatusFromDB(form.getImmId());
                    
                    // Build JSON response with form details
                    StringBuilder json = new StringBuilder();
                    json.append("{");
                    json.append("\"formId\":").append(form.getId()).append(",");
                    json.append("\"immId\":").append(form.getImmId()).append(",");
                    json.append("\"reqStatus\":\"").append(form.getReqStatus()).append("\",");
                    json.append("\"curStatus\":\"").append(curStatus != null ? curStatus : "Unknown").append("\"");
                    json.append("}");
                    
                    response.setStatus(HttpServletResponse.SC_OK);
                    out.println(json.toString());
                    System.out.println("ApprovalServlet.doGet: Successfully retrieved form for approval");
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.println("{\"error\":\"Form not found\"}");
                }
            } else {
                response.setStatus(HttpServletResponse.SC_NO_CONTENT);
                out.println("{\"message\":\"No pending forms in workflow\"}");
            }
            
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("{\"error\":\"" + e.getMessage() + "\"}");
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
            Approval approval = new Approval();
            
            // Create a temporary connection to retrieve the form
            App app = new App();
            try (Connection conn = app.getConnection()) {
                String formSql = "SELECT Imm_id, req_status FROM Forms WHERE Form_id = ?";
                int immId = -1;
                String reqStatus = null;
                
                try (PreparedStatement stmt = conn.prepareStatement(formSql)) {
                    stmt.setInt(1, formId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            immId = rs.getInt("Imm_id");
                            reqStatus = rs.getString("req_status");
                        } else {
                            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            out.println("{\"error\":\"Form not found\"}");
                            return;
                        }
                    }
                }
                
                Form form = new Form(reqStatus, immId, conn);
                form.setId(formId);
                approval = new Approval(form, false);
            }
            
            boolean success = false;
            String resultMessage = "";
            
            // Process the approval decision
            if ("approve".equalsIgnoreCase(decision)) {
                // Approve the form and notify immigration office
                approval.setNoErrors(true);
                success = approval.informImmigration();
                resultMessage = success ? "Form approved and submitted to immigration office" : "Failed to approve form";
                
            } else if ("returnReview".equalsIgnoreCase(decision)) {
                // Return the form back to review with notes
                approval.setNoErrors(false);
                success = approval.informReviewer();
                resultMessage = success ? "Form returned to reviewer" : "Failed to return form to reviewer";
                
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.println("{\"error\":\"Invalid decision. Must be 'approve' or 'returnReview'\"}");
                return;
            }
            
            // Send response
            if (success) {
                resp.setStatus(HttpServletResponse.SC_OK);
                StringBuilder response = new StringBuilder();
                response.append("{");
                response.append("\"success\":true,");
                response.append("\"message\":\"").append(resultMessage).append("\",");
                response.append("\"formId\":").append(formId).append(",");
                response.append("\"decision\":\"").append(decision).append("\"");
                response.append("}");
                out.println(response.toString());
                System.out.println("ApprovalServlet.doPost: Successfully processed " + decision + " for formId=" + formId);
            } else {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.println("{\"success\":false,\"error\":\"" + resultMessage + "\"}");
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


}
