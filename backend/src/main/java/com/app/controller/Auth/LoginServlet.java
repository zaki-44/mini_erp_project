package com.app.controller.Auth;

import com.erp.dao.implementation.user.UserDAO;
import com.erp.model.user.User;
import com.app.util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;
    private Gson gson;

    // Helper class to catch the incoming JSON
    private static class LoginRequest {
        String email;
        String password;
    }

    @Override
    public void init() {
        userDAO = new UserDAO();
        gson = new Gson();
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // 1. Setup Response Type (JSON)
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            // 2. Read JSON Body from React (NOT request.getParameter)
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            // Convert JSON string to Java Object
            LoginRequest loginRequest = gson.fromJson(sb.toString(), LoginRequest.class);

            // 3. Find User
            User user = userDAO.findByEmail(loginRequest.email);

            // 4. Validate Password
            if (user != null && user.getPasswordHash().equals(loginRequest.password) && user.isEmailVerified()) {
                
                // --- SUCCESS ---
                
               // 1. Generate JWT
                String token = JWTUtil.generateToken(user.getId(), user.getRole());

                // 2. CREATE COOKIE 
                Cookie jwtCookie = new Cookie("authToken", token);
                jwtCookie.setHttpOnly(true);  // Critical: JS cannot read this
                jwtCookie.setSecure(false);   // Set to TRUE if using HTTPS (Production)
                jwtCookie.setPath("/");       // Available for the whole app
                jwtCookie.setMaxAge(10 * 60 * 60); // 10 Hours (matches token expiry)

                // 3. Add Cookie to Response
                response.addCookie(jwtCookie);

                // 4. Send JSON (Only role/ID needed now, NO TOKEN)
                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("role", user.getRole()); 
                jsonResponse.addProperty("userId", user.getId());

                response.setStatus(HttpServletResponse.SC_OK);

            } 
            else if (user != null && !user.isEmailVerified()) {
                
                // --- EMAIL NOT VERIFIED ---
                
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Email not verified");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            }
            else {
                
                // --- FAILURE ---
                
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Invalid email or password");
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
            }

        } catch (SQLException e) {
            e.printStackTrace();
            jsonResponse.addProperty("status", "error");
            jsonResponse.addProperty("message", "Database error");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500
        }

        // 5. Send Final JSON back to React
        response.getWriter().write(gson.toJson(jsonResponse));
    }
}