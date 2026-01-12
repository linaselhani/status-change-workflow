package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Immigrant{

    private int id;
    private String curStatus;
    private int[] deps;
    private Connection conn;

    public Immigrant(int id, String curStatus, int[] deps, Connection conn){
        this.id = id;
        this.curStatus = curStatus;
        this.deps = deps;
        this.conn = conn;

        int check = addToDB();
        if (check == 0) {
            throw new IllegalStateException("Unable to add Immigrant to DB.");
        }
        
    }

    /*
     * addToDB uses all of the instantiated fields of this Immigrant object to create a new data entry in the table Immigrants
     * 
     */
    public int addToDB(){
        boolean hasDependentIds = hasDependentIdsColumn();
        String sql = hasDependentIds
                ? "INSERT INTO Immigrants (id, cur_status, dependent_ids) VALUES (?, ?, ?)"
                : "INSERT INTO Immigrants (id, cur_status) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, curStatus);
            if (hasDependentIds) {
                stmt.setArray(3, toSqlArray(deps));
            }

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("SUCCESSFULLY INSERTED IMMIGRANT");
            }

            if (!hasDependentIds && deps != null && deps.length > 0) {
                String depSql = "INSERT INTO Deps_of_Imms (Imm_id, Dep_id) VALUES (?, ?)";
                try (PreparedStatement depStmt = conn.prepareStatement(depSql)) {
                    for (int depId : deps) {
                        depStmt.setInt(1, id);
                        depStmt.setInt(2, depId);
                        depStmt.addBatch();
                    }
                    depStmt.executeBatch();
                }
            }

            return id;
        } catch (SQLException e) {
            throw new IllegalStateException("Immigrant insert failed: " + e.getMessage(), e);
        }
    }


    // Add in getters and setters for all parameters in Immigrant Constructor

    /* GETTER METHODS */

    public int getId(){
        return this.id;
    }

    public String getCurStatus(){
        return this.curStatus;
    }

    public String getDeps(){
        if(deps == null){
            return "null";
        }

        String stringDeps = "" + deps[0];
        for(int i = 1; i < deps.length; i++){
            stringDeps += "\n" + deps[i];
        }

        return stringDeps;
    }


    /* UPDATE or SETTER METHODS */
    public void updateCurStatus(int id, String newStatus) {
        String sql = "UPDATE Immigrants SET cur_status = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newStatus);
            stmt.setInt(2, id);
            this.curStatus = newStatus;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
    public void updateDeps(int id, int[] newDeps) {
        if (hasDependentIdsColumn()) {
            String sql = "UPDATE Immigrants SET dependent_ids = ? WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setArray(1, toSqlArray(newDeps));
                stmt.setInt(2, id);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
            this.deps = newDeps;
            return;
        }

        String deleteSql = "DELETE FROM Deps_of_Imms WHERE Imm_id = ?";
        String insertSql = "INSERT INTO Deps_of_Imms (Imm_id, Dep_id) VALUES (?, ?)";
        try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, id);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        if (newDeps != null && newDeps.length > 0) {
            try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                for (int depId : newDeps) {
                    insertStmt.setInt(1, id);
                    insertStmt.setInt(2, depId);
                    insertStmt.addBatch();
                }
                insertStmt.executeBatch();
            } catch (SQLException e) {
                e.printStackTrace();
                return;
            }
        }
        this.deps = newDeps;
    }
    
    // for only 1 dependent
    public void updateDeps(int id, int depId) {
        int[] newDeps = new int[1];  
        newDeps[0] = depId;          
        updateDeps(id, newDeps);     
    }


    public void updateImmigrant(int id, String newStatus, int[] newDeps) {
        updateCurStatus(id, newStatus);
        updateDeps(id, newDeps);
    }

    private boolean hasDependentIdsColumn() {
        String sql = "SELECT 1 FROM information_schema.columns WHERE table_name = 'immigrants' AND column_name = 'dependent_ids'";
        try (PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            return rs.next();
        } catch (SQLException e) {
            return false;
        }
    }

    private java.sql.Array toSqlArray(int[] values) throws SQLException {
        if (values == null || values.length == 0) {
            return null;
        }
        Integer[] boxed = new Integer[values.length];
        for (int i = 0; i < values.length; i++) {
            boxed[i] = values[i];
        }
        return conn.createArrayOf("INTEGER", boxed);
    }
}
