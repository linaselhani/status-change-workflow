package edu.gmu.cs321;

import java.io.IOException;
import java.sql.Connection;
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

//test
public class AppServlet extends HttpServlet {

    /** doGet's purpose is to only uses the connection to pull info from the database. 
     * request: this variable is essentially what the "client" wants from this code. client: us whenever we interact with the db 
     *      example: client is requesting the first name of a Person object in the database -- this is the request
     *      The request formatting depends on your html script(s) code: 1) how did you format the html data(usually either String or JSON) 
     *                                                                  2) how did you send your formatted data to your servlet  
     * response: this variable is used to repond back to the client/website. if we continue the example, response should return the first name.
     *          note: while a response is not always necessary for every method(like delete), try to create one anyway because its good for testing.
    */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)throws IOException {

        App app = new App();
        
        try (Connection conn = app.getConnection()) {
            if (conn != null) {
                System.out.println("Connected to PostgreSQL successfully!");
            }

            /* START OF DATABASE RELATED OPERATIONS */
            

            // Do NOT use this class or method because it will be DELETED towards the end of the project.


            /* END OF OPERATIONS */
            conn.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        
    }

    /* doPost is meant for creating a new object BUT it can also be used to update the fields of objects*/
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)throws IOException {

    }

    /* doDelete is meant for deleting objects or fields/parameters */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)throws IOException {

    }


}