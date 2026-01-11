package edu.gmu.cs321;

//You do not need to modify this class until we implement the system on GMU servers

//for sending to wf servlet
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//to help read response - also added to POM
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class WFUtil {

public static Integer addformtoWF (Integer formid, String nextstep, Integer groupid) {
//this method should be moved to the same class as getWFform method
        Integer resp_code = 0;
	//to-do update URL in Sprint 3 with target Zeus connection
        String apiUrl = "http://localhost:8080/workflow_updated/addWFItem"; 

        // Prepare key-value pairs
        Map<Object, Object> data = new HashMap<>();
        data.put("form_id", formid);
        data.put("step_name", nextstep);
        data.put("group_id", groupid);

        try {
            // Build the form-urlencoded body
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<Object, Object> entry : data.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                formBody.append("=");
                formBody.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Build HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                  
              ObjectMapper objectMapper = new ObjectMapper();
              
                // Parse the JSON string into a HashMap
                Map<String, Object> dataMap = objectMapper.readValue(
                    response.body(), new TypeReference<Map<String, Object>>() {}
                );

                // Access values using keys
                resp_code = (Integer) dataMap.get("status");


            // Print response details
            System.out.println("Status Code: " + resp_code);
            System.out.println("Response Body: " + response.body());

            

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp_code;
    }

public static Integer getformfromWF (String nextstep, Integer groupid) {

        Integer resp_code = 0;
        //to-do update URL in Sprint 3 with target Zeus connection
        String apiUrl = "http://localhost:8080/workflow_updated/getNextWFItem"; 

        // Prepare key-value pairs
        Map<Object, Object> data = new HashMap<>();
        data.put("step_name", nextstep);
        data.put("group_id", groupid);

        try {
            // Build the form-urlencoded body
            StringBuilder formBody = new StringBuilder();
            for (Map.Entry<Object, Object> entry : data.entrySet()) {
                if (formBody.length() > 0) {
                    formBody.append("&");
                }
                formBody.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
                formBody.append("=");
                formBody.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            }

            // Create HttpClient
            HttpClient client = HttpClient.newHttpClient();

            // Build HttpRequest
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(formBody.toString()))
                    .build();

            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                  
              ObjectMapper objectMapper = new ObjectMapper();
              
                // Parse the JSON string into a HashMap
                Map<String, Object> dataMap = objectMapper.readValue(
                    response.body(), new TypeReference<Map<String, Object>>() {}
                );

                // Access values using keys
                resp_code = (Integer) dataMap.get("status");


            // Print response details
            System.out.println("Status Code: " + resp_code);
            System.out.println("Response Body: " + response.body());

            

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resp_code;
    }
}