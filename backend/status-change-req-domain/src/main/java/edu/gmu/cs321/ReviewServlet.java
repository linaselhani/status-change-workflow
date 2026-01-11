package edu.gmu.cs321;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Servlet for the REVIEWER portal.
 *
 * doGet  - given ?formId=1, returns JSON for that review form
 * doPost - updates reviewer fields (comments, workflow, etc.) for that form
 */
public class ReviewServlet extends HttpServlet {

    // Your real table
    private static final String TABLE_NAME = "public.review_forms";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String formIdParam = request.getParameter("formId");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (formIdParam == null || formIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Missing formId parameter\"}");
            }
            return;
        }

        int formId;
        try {
            formId = Integer.parseInt(formIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"formId must be an integer\"}");
            }
            return;
        }

        String sql =
                "SELECT form_id, applicant_name, applicant_id, current_status, " +
                "       requested_status, reviewer_comments, workflow_status, date_submitted " +
                "FROM " + TABLE_NAME + " " +
                "WHERE form_id = ?";

        try (Connection conn = App.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, formId);

            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    try (PrintWriter out = response.getWriter()) {
                        out.write("{\"error\":\"Form not found\"}");
                    }
                    return;
                }

                String json = "{"
                        + "\"formId\":" + rs.getInt("form_id") + ","
                        + "\"applicantName\":\"" + escapeJson(rs.getString("applicant_name")) + "\","
                        + "\"applicantId\":\"" + escapeJson(rs.getString("applicant_id")) + "\","
                        + "\"currentStatus\":\"" + escapeJson(rs.getString("current_status")) + "\","
                        + "\"requestedStatus\":\"" + escapeJson(rs.getString("requested_status")) + "\","
                        + "\"reviewerComments\":\"" + escapeJson(rs.getString("reviewer_comments")) + "\","
                        + "\"workflowStatus\":\"" + escapeJson(rs.getString("workflow_status")) + "\","
                        + "\"dateSubmitted\":\"" + escapeJson(rs.getString("date_submitted")) + "\""
                        + "}";

                response.setStatus(HttpServletResponse.SC_OK);
                try (PrintWriter out = response.getWriter()) {
                    out.write(json);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Database error while loading form\"}");
            }
        }
    }

    /**
     * Reviewer updates the form.
     *
     * Expected parameters from your HTML/JS:
     *  - formId
     *  - applicantName
     *  - applicantId
     *  - currentStatus
     *  - requestedStatus
     *  - reviewerComments
     *  - workflowStatus  (e.g. "under_review", "returned", "approved")
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String formIdParam      = request.getParameter("formId");
        String applicantName    = request.getParameter("applicantName");
        String applicantId      = request.getParameter("applicantId");
        String currentStatus    = request.getParameter("currentStatus");
        String requestedStatus  = request.getParameter("requestedStatus");
        String reviewerComments = request.getParameter("reviewerComments");
        String workflowStatus   = request.getParameter("workflowStatus");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        if (formIdParam == null || formIdParam.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Missing formId parameter\"}");
            }
            return;
        }

        int formId;
        try {
            formId = Integer.parseInt(formIdParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"formId must be an integer\"}");
            }
            return;
        }

        String updateSql =
                "UPDATE " + TABLE_NAME + " SET "
              + "applicant_name = ?, "
              + "applicant_id = ?, "
              + "current_status = ?, "
              + "requested_status = ?, "
              + "reviewer_comments = ?, "
              + "workflow_status = ? "
              + "WHERE form_id = ?";

        try (Connection conn = App.getConnection();
             PreparedStatement ps = conn.prepareStatement(updateSql)) {

            ps.setString(1, applicantName);
            ps.setString(2, applicantId);
            ps.setString(3, currentStatus);
            ps.setString(4, requestedStatus);
            ps.setString(5, reviewerComments);
            ps.setString(6, workflowStatus);
            ps.setInt(7, formId);

            int updated = ps.executeUpdate();

            if (updated == 0) {
                // Nothing was updated – form_id didn't exist
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"error\":\"Form not found for update\"}");
                }
                return;
            }

            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"status\":\"ok\"}");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Database error while saving form\"}");
            }
        }
    }

    /** Escape JSON meta characters */
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}


