package com.app.controller.Api.client;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.SQLException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.app.dao.Implementation.DelivererDAO;
import com.app.dao.Implementation.AffectationDAO;
import com.app.dao.Implementation.DeliveryPackageDAO;
import com.app.model.Deliverer;
import com.app.model.Affectation;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.AffectationStatus;
import com.app.model.Enums.PackageStatus;
import java.util.List;

/**
 * Servlet for handling deliverer rating requests from clients.
 * 
 * Endpoint: POST /api/client/rate-deliverer
 * 
 * This servlet allows authenticated clients to rate deliverers after a completed delivery.
 * The rating is stored in the deliverer's rate field in the database.
 * 
 * Note: lazm nbdlou mnb33d ndirou table t3 average rate 
 * 
 */
@WebServlet("/api/client/rate-deliverer")
public class RateDeliverServlet extends HttpServlet {

    // DAO instances 3lajal  database operations
    private DelivererDAO delivererDAO = new DelivererDAO();
    private AffectationDAO affectationDAO = new AffectationDAO();
    private DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();
    
    // Gson instance for JSON serialization/deserialization Object ➜ JSON w l3eeks
    private Gson gson = new Gson();

    /**
     * Helper class to capture the incoming JSON request body
     
     */
    private static class RatingRequest {
        int delivererId;   
        double rating;        
        Integer packageId;    // machi lazm
    }

    /**
     * Initialize servlet resources
     * Called once when the servlet is first loaded
     */
    @Override
    public void init() {
        // Initialize DAOs and Gson
       
    }

    /**
     *POST requests to rate a deliverer
     * 
     * Expected : 
     * {
     *   "delivererId": 123,
     *   "rating": 4.5,
     *   "packageId": 456  // optional
     * }
     * 
     * @param request HTTP request containing rating data
     * @param response HTTP response to send back
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        // Step 1: Set response content type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        //  JSON response object
        JsonObject jsonResponse = new JsonObject();
        PrintWriter out = response.getWriter();

        try {
            // Step 2: Get authenticated user ID from JWT filter
            // The JwtFilter sets this attribute after validating the token
            Object userIdObj = request.getAttribute("userId");
            if (userIdObj == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Unauthorized: User not authenticated");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print(gson.toJson(jsonResponse));
                return;
            }
            
            int clientId = (Integer) userIdObj;
            
            // Step 3: Get user role from JWT filter
            // Verify that the user is a CLIENT (only clients can rate deliverers)
            String userRole = (String) request.getAttribute("userRole");
            if (userRole == null || !"CLIENT".equals(userRole)) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Forbidden: Only clients can rate deliverers");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // Step 4: Read JSON request body from the client
            // Build the request body string by reading all lines
            StringBuilder requestBody = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }

            // Step 5: Parse JSON request body into RatingRequest object
            RatingRequest ratingRequest = gson.fromJson(requestBody.toString(), RatingRequest.class);

            // Step 6: Validate request data
            if (ratingRequest == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid request: Request body is empty or invalid JSON");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // Validate deliverer ID is provided
            if (ratingRequest.delivererId <= 0) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid request: delivererId is required and must be positive");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // Validate rating value is within acceptable range (1.0 to 5.0)
            if (ratingRequest.rating < 1.0 || ratingRequest.rating > 5.0) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid request: Rating must be between 1.0 and 5.0");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print(gson.toJson(jsonResponse));
                return;
            }

            // Step 7: Verify that the deliverer exists in the database
            Deliverer deliverer = delivererDAO.findById(ratingRequest.delivererId);
            if (deliverer == null) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Deliverer not found");
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print(gson.toJson(jsonResponse));
                return;
            }

           

                // Verify the package belongs to the authenticated client
                if (pkg.getIdClientSource() != clientId) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "Forbidden: You can only rate deliverers for your own packages");
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    out.print(gson.toJson(jsonResponse));
                    return;
                }

                // Verify the package was delivered (status is DELIVERED)
                if (pkg.getStatus() != PackageStatus.DELIVERED) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "You can only rate deliverers for completed deliveries");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(jsonResponse));
                    return;
                }

                // Verify there's a completed affectation for this package and deliverer
                List<Affectation> affectations = affectationDAO.findByDelivererId(ratingRequest.delivererId);
                boolean hasValidAffectation = false;
                for (Affectation aff : affectations) {
                    if (aff.getIdPackage() == ratingRequest.packageId 
                        && aff.getStatus() == AffectationStatus.COMPLETED) {
                        hasValidAffectation = true;
                        break;
                    }
                }

                if (!hasValidAffectation) {
                    jsonResponse.addProperty("status", "error");
                    jsonResponse.addProperty("message", "No completed delivery found for this deliverer and package");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print(gson.toJson(jsonResponse));
                    return;
                }
            }

            // For now, we'll update the rate directly with the new rating
            // In a real system, you'd calculate: newAverage = (oldAverage * oldCount + newRating) / (oldCount + 1)
            deliverer.setRate(ratingRequest.rating);
            
            // Step 10: Save the updated deliverer to the database
            delivererDAO.update(deliverer);

            // Step 11: Prepare success response
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", "Rating submitted successfully");
            jsonResponse.addProperty("delivererId", ratingRequest.delivererId);
            jsonResponse.addProperty("rating", ratingRequest.rating);
            jsonResponse.addProperty("updatedRate", deliverer.getRate());
            
            response.setStatus(HttpServletResponse.SC_OK);

        } catch (SQLException e) {
            // Step 12: Handle database errors
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            
        } catch (NumberFormatException e) {
            // Step 13: Handle number format errors
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Invalid number format in request");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            
        } catch (Exception e) {
            // Step 14: Handle any other unexpected errors
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Unexpected error: " + e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        // Step 15: Send the JSON response back to the client
        out.print(gson.toJson(jsonResponse));
    }
}

