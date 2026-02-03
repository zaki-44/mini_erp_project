package com.app.controller.Auth;

import com.app.dao.Implementation.UserDAO;
import com.app.model.User;
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
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        JsonObject jsonResponse = new JsonObject();

        try {
            StringBuilder sb = new StringBuilder();
            BufferedReader reader = request.getReader();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            
            LoginRequest loginRequest = gson.fromJson(sb.toString(), LoginRequest.class);

            User user = userDAO.getByEmail(loginRequest.email);
            if (user != null && user.getPasswordHash().equals(loginRequest.password) && user.isEmailVerified()) {
                
                String token = JWTUtil.generateToken(user.getId(), user.getRole());

                Cookie jwtCookie = new Cookie("authToken", token);
                jwtCookie.setHttpOnly(true);  
                jwtCookie.setSecure(false);  
                jwtCookie.setPath("/");       
                jwtCookie.setMaxAge(10 * 60 * 60); 

                response.addCookie(jwtCookie);

                jsonResponse.addProperty("status", "success");
                jsonResponse.addProperty("role", user.getRole()); 
                jsonResponse.addProperty("userId", user.getId());

                response.setStatus(HttpServletResponse.SC_OK);

            } 
            else if (user != null && !user.isEmailVerified()) {
                jsonResponse.addProperty("status", "error");
                jsonResponse.addProperty("message", "Email not verified");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403
            }
            else {
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

        response.getWriter().write(gson.toJson(jsonResponse));
    }
}