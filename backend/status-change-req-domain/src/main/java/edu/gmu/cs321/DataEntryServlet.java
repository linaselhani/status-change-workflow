package edu.gmu.cs321;
import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DataEntryServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
        String formIdParam = req.getParameter("formId");
        String fname = req.getParameter("fname");
        String lname = req.getParameter("lname");
        String dobStr = req.getParameter("dob");
        String curStatus = firstNonBlank(req.getParameter("curStatus"), req.getParameter("current-status"));
        String reqStatus = firstNonBlank(req.getParameter("reqStatus"), req.getParameter("requested-status"));
        String depStr = firstNonBlank(req.getParameter("dependents"), req.getParameter("deps"));

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (fname == null || lname == null || dobStr == null || curStatus == null || reqStatus == null) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().write("{\"error\":\"Missing required fields\"}");
            return;
        }

        Date dob = Date.valueOf(dobStr);
        int[] deps = parseDeps(depStr);

        try (Connection conn = App.getConnection()) {
            conn.setAutoCommit(false);

            if (formIdParam != null && !formIdParam.isBlank()) {
                int formId = Integer.parseInt(formIdParam);
                int immId = loadImmId(conn, formId);
                if (immId == 0) {
                    resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    resp.getWriter().write("{\"error\":\"Form not found\"}");
                    return;
                }

                updatePerson(conn, immId, fname, lname, dob);
                updateImmigrant(conn, immId, curStatus);
                updateForm(conn, formId, reqStatus);
                updateDeps(conn, immId, deps);
                moveForm(conn, "Data_Entry_Forms", "Review_Forms", formId);

                conn.commit();
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.getWriter().write("{\"status\":\"resubmitted\",\"formId\":" + formId + "}");
                return;
            }

            Person person = new Person(fname, lname, dob, conn);
            Immigrant imm = new Immigrant(person.getId(), curStatus, deps, conn);
            Form form = new Form(reqStatus, imm.getId(), conn);

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO Review_Forms (Form_id) VALUES (?)")) {
                stmt.setInt(1, form.getId());
                stmt.executeUpdate();
            }

            conn.commit();
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.getWriter().write("{\"status\":\"created\",\"formId\":" + form.getId() + "}");
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"Database error: " + escapeJson(e.getMessage()) + "\"}");
        } catch (RuntimeException e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("{\"error\":\"" + escapeJson(e.getMessage()) + "\"}");
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }

    private static int[] parseDeps(String depStr) {
        if (depStr == null || depStr.isBlank()) {
            return null;
        }
        String[] depArr = depStr.split(",");
        List<Integer> parsed = new ArrayList<>();
        for (String dep : depArr) {
            String trimmed = dep.trim();
            if (!trimmed.isEmpty()) {
                parsed.add(Integer.parseInt(trimmed));
            }
        }
        if (parsed.isEmpty()) {
            return null;
        }
        int[] deps = new int[parsed.size()];
        for (int i = 0; i < parsed.size(); i++) {
            deps[i] = parsed.get(i);
        }
        return deps;
    }

    private static int loadImmId(Connection conn, int formId) throws SQLException {
        String sql = "SELECT Imm_id FROM Forms WHERE Form_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, formId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("Imm_id");
                }
            }
        }
        return 0;
    }

    private static void updatePerson(Connection conn, int personId, String fname, String lname, Date dob)
            throws SQLException {
        String sql = "UPDATE Persons SET first_name = ?, last_name = ?, dob = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fname);
            stmt.setString(2, lname);
            stmt.setDate(3, dob);
            stmt.setInt(4, personId);
            stmt.executeUpdate();
        }
    }

    private static void updateImmigrant(Connection conn, int immId, String curStatus) throws SQLException {
        String sql = "UPDATE Immigrants SET cur_status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, curStatus);
            stmt.setInt(2, immId);
            stmt.executeUpdate();
        }
    }

    private static void updateForm(Connection conn, int formId, String reqStatus) throws SQLException {
        String sql = "UPDATE Forms SET req_status = ?, return_reason = NULL WHERE Form_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reqStatus);
            stmt.setInt(2, formId);
            stmt.executeUpdate();
        }
    }

    private static void updateDeps(Connection conn, int immId, int[] deps) throws SQLException {
        if (hasDependentIdsColumn(conn)) {
            String sql = "UPDATE Immigrants SET dependent_ids = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setArray(1, toSqlArray(conn, deps));
                stmt.setInt(2, immId);
                stmt.executeUpdate();
            }
            return;
        }

        String deleteSql = "DELETE FROM Deps_of_Imms WHERE Imm_id = ?";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, immId);
            deleteStmt.executeUpdate();
        }

        if (deps == null || deps.length == 0) {
            return;
        }

        String insertSql = "INSERT INTO Deps_of_Imms (Imm_id, Dep_id) VALUES (?, ?)";
        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
            for (int depId : deps) {
                insertStmt.setInt(1, immId);
                insertStmt.setInt(2, depId);
                insertStmt.addBatch();
            }
            insertStmt.executeBatch();
        }
    }

    private static void moveForm(Connection conn, String fromTable, String toTable, int formId) throws SQLException {
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

    private static boolean hasDependentIdsColumn(Connection conn) throws SQLException {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_name = 'immigrants' AND column_name = 'dependent_ids'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        }
    }

    private static java.sql.Array toSqlArray(Connection conn, int[] values) throws SQLException {
        if (values == null || values.length == 0) {
            return null;
        }
        Integer[] boxed = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        return conn.createArrayOf("INTEGER", boxed);
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

}
