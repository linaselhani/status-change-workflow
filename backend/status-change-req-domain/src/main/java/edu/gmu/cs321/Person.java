package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Person {
    
    private int id = 0;
    private String fName;
    private String lName;
    private Date dob;
    private Connection conn;



    /*
     * Person constructor
     */
    public Person(String fName, String lName, Date dob, Connection conn){

        this.fName = fName;
        this.lName = lName;
        this.dob = dob;
        this.conn = conn;

        int createdId = addToDB();
        if (createdId == 0) {
            throw new IllegalStateException("Unable to add Person to DB.");
        }
        this.id = createdId;
    }



    /*
     * addToDB uses all of the instantiated fields of this Person object to create a new data entry in the table Persons
     * returns created entry's ID
     */
    public int addToDB(){

        String sql = "INSERT INTO public.persons (first_name, last_name, dob) VALUES (?, ?, ?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fName);
            stmt.setString(2, lName);
            stmt.setDate(3, dob);
            try (ResultSet res = stmt.executeQuery()) {
                if (res.next()) {
                    System.out.println("SUCCESSFULLY INSERTED PERSON");
                    return res.getInt("id");
                }
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Person insert failed: " + e.getMessage(), e);
        }
        
        return 0;
    }

    /*
    * Getters and setters for Person fields
    */

    /* GETTER or CREATE METHODS */
    public int getId(){
        return id;
    }

    public String getFirstName(){
        return fName;
    }

    public String getLastName(){
        return lName;
    }

    public Date getDOB(){
        return dob;
    }

    public Person createPerson(int id){
        
        String sql = "SELECT first_name, FROM persons WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setInt(1, id);
            //stmt.setString(1, id);
            this.lName = lName;
        }catch(SQLException e){
            e.printStackTrace();
        }

        return this;
    }

    /* UPDATE or SETTER METHODS */
    public void updateFirstName(int id, String fName){
        String sql = "UPDATE persons SET first_name = ? WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, fName);
            stmt.setInt(2, id);
            this.fName = fName;
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void updateLastName(int id, String lName){
        String sql = "UPDATE persons SET last_name = ? WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setString(1, lName);
            stmt.setInt(2, id);
            this.lName = lName;
        }catch(SQLException e){
            e.printStackTrace();
        }
    } 

    public void updateDOB(int id, Date dob){
        String sql = "UPDATE persons SET dob = ? WHERE id = ?";
        try(PreparedStatement stmt = conn.prepareStatement(sql)){
            stmt.setDate(1, dob);
            stmt.setInt(2, id);
            this.dob = dob;
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public void updatePerson(int id, String newFName, String newLName, Date dob){
        updateFirstName(id, newFName);
        updateLastName(id, newLName);
        updateDOB(id, dob);
    }

}
