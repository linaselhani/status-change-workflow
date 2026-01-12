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

    private static final String REVIEW_TABLE = "Review_Forms";

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

        try (Connection conn = App.getConnection()) {
            if (!isInStage(conn, REVIEW_TABLE, formId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                try (PrintWriter out = response.getWriter()) {
                    out.write("{\"error\":\"Form not found in review\"}");
                }
                return;
            }

            String json = loadFormJson(conn, formId);
            response.setStatus(HttpServletResponse.SC_OK);
            try (PrintWriter out = response.getWriter()) {
                out.write(json);
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

        String formIdParam = request.getParameter("formId");
        String decision = request.getParameter("decision");
        String returnReason = request.getParameter("returnReason");

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

        if (decision == null || decision.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try (PrintWriter out = response.getWriter()) {
                out.write("{\"error\":\"Missing decision parameter\"}");
            }
            return;
        }

        try (Connection conn = App.getConnection()) {
            conn.setAutoCommit(false);

            if (!isInStage(conn, REVIEW_TABLE, formId)) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"Form not found in review\"}");
                return;
            }

            if ("approve".equalsIgnoreCase(decision)) {
                clearReturnReason(conn, formId);
                moveForm(conn, REVIEW_TABLE, "Approval_Forms", formId);
            } else if ("return".equalsIgnoreCase(decision)) {
                if (returnReason == null || returnReason.isBlank()) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.getWriter().write("{\"error\":\"returnReason is required when returning\"}");
                    return;
                }
                updateReturnReason(conn, formId, returnReason);
                moveForm(conn, REVIEW_TABLE, "Data_Entry_Forms", formId);
            } else {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"Invalid decision\"}");
                return;
            }

            conn.commit();
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
}


