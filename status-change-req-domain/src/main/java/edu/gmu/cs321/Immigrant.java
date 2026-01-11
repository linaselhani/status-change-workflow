package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Array;

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
        if(check == 0){
            System.out.println("ERROR OCCURRED. Unable to add Immigrant to DB.");
            System.exit(-1); 
        }
        
    }

    /*
     * addToDB uses all of the instantiated fields of this Immigrant object to create a new data entry in the table Immigrants
     * 
     */
    public int addToDB(){
        String sql = "INSERT INTO Immigrants (id, cur_status, dependent_ids) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.setString(2, curStatus);

            Integer[] boxed = null;
            if (deps != null) {
                boxed = new Integer[deps.length];
                for (int i = 0; i < deps.length; i++) {
                    boxed[i] = deps[i];
                }
            }

            java.sql.Array depArray = null;
            if (boxed != null) {
                depArray = conn.createArrayOf("INTEGER", boxed);
            }

            stmt.setArray(3, depArray);

            int rows = stmt.executeUpdate();
            if (rows > 0) {
                System.out.println("SUCCESSFULLY INSERTED IMMIGRANT");
                return id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return 0;
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
        String sql = "UPDATE Immigrants SET dependent_ids = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {

            Integer[] boxed = null;
            if (newDeps != null) {
                boxed = new Integer[newDeps.length];
                for (int i = 0; i < newDeps.length; i++) {
                    boxed[i] = newDeps[i];
                }
            }

            Array depArray = null;
            if (boxed != null) {
                depArray = conn.createArrayOf("INTEGER", boxed);
            }

            stmt.setArray(1, depArray);
            stmt.setInt(2, id);

            stmt.executeUpdate();
            this.deps = newDeps;
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
}
