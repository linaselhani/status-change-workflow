package edu.gmu.cs321;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.sql.Date;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class DataEntryServlet extends HttpServlet {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws ServletException, IOException {
        App app = new App();
        
        // person
        String fname = req.getParameter("fname");
        String lname = req.getParameter("lname");
        
        String dobStr = req.getParameter("dob");
        Date dob = Date.valueOf(dobStr);
        
        // immigrant
        String curStatus = req.getParameter("current-status");
        
        // dep
        int[] deps = null;
        
        if(req.getParameter("dependents") != null){
            String depStr = req.getParameter("dependents");
            String[] depArr = depStr.split(",");
            deps = new int[depArr.length];
            for (int i = 0; i < deps.length; i++) {
                deps[i] = Integer.parseInt(depArr[i].trim());
            }
        }
        // form
        String reqStatus = req.getParameter("requested-status");
        
        try (Connection conn = app.getConnection()) {
            if (conn != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            }
            Person person = new Person(fname, lname, dob, conn);
            Immigrant imm = new Immigrant(person.getId(), curStatus, deps, conn);
            Form form = new Form(reqStatus, imm.getId(), conn);
                        
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }

}