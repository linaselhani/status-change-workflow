package edu.gmu.cs321;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Form {

    private int form_id;
    private String reqStatus;
    private int imm_id;
    private Connection conn;


    public Form(String reqStatus, int imm_id, Connection conn){
        // initialized connection field
        this.reqStatus = reqStatus;
        this.imm_id = imm_id;
        this.conn = conn;

        int check = addToDB();
        if(check == 0){
            System.out.println("ERROR OCCURRED. Unable to add Form to DB.");
            System.exit(-1); 
        }

    }

    /* GETTER METHODS */
    public int getId(){
        return form_id;
    }

    public String getReqStatus(){
        return reqStatus;
    }

    public Connection getConn(){
        return conn;
    }

    public int getImmId(){
        return imm_id;
    }





    /* SETTER METHODS */



    public void setReqStatus(String reqStatus){
        this.reqStatus = reqStatus;
    }

    
    public void setId(int form_id) {
        this.form_id = form_id;
    }

    public void setImmId(int imm_id) {
        this.imm_id = imm_id;
    }

    public void setConn(Connection conn) {
        this.conn = conn;
    }

    /**
     * addToDB inserts a new form into the Forms table using the reqStatus and imm_id fields
     * @return the generated form_id on success, or 0 on failure
     */
    public int addToDB() {
        String sql = "INSERT INTO Forms (Imm_id, req_status) VALUES (?, ?) RETURNING Form_id";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, imm_id);
            stmt.setString(2, reqStatus);
            
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                this.form_id = rs.getInt("Form_id");
                System.out.println("SUCCESSFULLY INSERTED FORM");
                return this.form_id;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return 0;
    }

}
