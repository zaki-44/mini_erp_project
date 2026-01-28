package com.app.controller.Api.client;


import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.PackageStatus;
import com.app.model.Enums.VehicleType;
import com.app.util.Database;

// this is the servlet for getting package history
@WebServlet("/api/client/packages/history")
public class packages_hist extends HttpServlet {

    // converting to JSON
    private Gson gson = new Gson();

    // handles GET requests
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        // response type to json
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        
        // writer to write response
        PrintWriter out = resp.getWriter();

        try {
            //  get the user id from the request
            // the JWT filter puts it there
            Integer clientId = (Integer) req.getAttribute("userId");
            
            // user is logged in wela lala
            if (clientId == null) {
                //  not logged in :
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"User not authenticated\"}");
                return;
            }

            //  get all packages from database
            // a list to store packages
            List<DeliveryPackage> packagesList = new ArrayList<DeliveryPackage>();
            
            // creating connection to database
            Connection connection = null;
            PreparedStatement statement = null;
            ResultSet resultSet = null;
            
            try {
                // getting database connection
                connection = Database.getConnection();
                
                // SQL query to get packages where client is the source
                String sqlQuery = "SELECT * FROM package WHERE id_client_source = ? ORDER BY created_at DESC";
                
                //  the statement
                statement = connection.prepareStatement(sqlQuery);
                
                // the client id parameter
                statement.setInt(1, clientId);
                
                // executing the query
                resultSet = statement.executeQuery();
                
                // looping through all results
                while (resultSet.next()) {
                    // creating new package object for each row
                    DeliveryPackage pkg = new DeliveryPackage();
                    
                    // getting id_package from database
                    int idPackage = resultSet.getInt("id_package");
                    pkg.setIdPackage(idPackage);
                    
                    // getting id_client_source from database
                    int idClientSource = resultSet.getInt("id_client_source");
                    pkg.setIdClientSource(idClientSource);
                    
                    // getting id_client_destination from database
                    // this can be null so need to check
                    int idClientDestination = resultSet.getInt("id_client_destination");
                    if (!resultSet.wasNull()) {
                        pkg.setIdClientDestination(idClientDestination);
                    }
                    
                    // getting vehicle type from database
                    String vehicleType = resultSet.getString("vehicle_type_needed");
                    if (vehicleType != null) {
                        // converting string to enum
                        VehicleType vt = VehicleType.valueOf(vehicleType);
                        pkg.setVehicleTypeNeeded(vt);
                    }
                    
                    // getting address source
                    String addressSource = resultSet.getString("address_source");
                    pkg.setAddressSource(addressSource);
                    
                    // getting address destination
                    String addressDestination = resultSet.getString("address_destination");
                    pkg.setAddressDestination(addressDestination);
                    
                    // getting weight
                    double weight = resultSet.getDouble("weight");
                    pkg.setWeight(weight);
                    
                    // getting price
                    double price = resultSet.getDouble("price");
                    pkg.setPrice(price);
                    
                    // getting dimensions
                    String dimensions = resultSet.getString("dimensions");
                    pkg.setDimensions(dimensions);
                    
                    // getting description
                    String description = resultSet.getString("description");
                    pkg.setDescription(description);
                    
                    // getting delivery instructions
                    String deliveryInstructions = resultSet.getString("delivery_instructions");
                    pkg.setDeliveryInstructions(deliveryInstructions);
                    
                    // getting status
                    String statusString = resultSet.getString("status");
                    if (statusString != null) {
                        // converting string to enum
                        PackageStatus status = PackageStatus.valueOf(statusString);
                        pkg.setStatus(status);
                    }
                    
                    // getting created_at timestamp
                    Timestamp createdAt = resultSet.getTimestamp("created_at");
                    pkg.setCreatedAt(createdAt);
                    
                    // adding package to the list
                    packagesList.add(pkg);
                }
                
            } finally {
                // closing resources to avoid memory leaks
                if (resultSet != null) {
                    try {
                        resultSet.close();
                    } catch (SQLException e) {
                        // just printing error if close fails
                        e.printStackTrace();
                    }
                }
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        // just printing error if close fails
                        e.printStackTrace();
                    }
                }
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        // just printing error if close fails
                        e.printStackTrace();
                    }
                }
            }
            
            // converting list to JSON
            String jsonResponse = gson.toJson(packagesList);
            
            // sending success response
            resp.setStatus(HttpServletResponse.SC_OK);
            out.print(jsonResponse);

        } catch (SQLException e) {
            // if there is a database error
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            // if there is any other error
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Unexpected error: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }
}
