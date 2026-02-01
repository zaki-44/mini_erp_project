package com.app.controller.Auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Invalidate the authToken cookie
        Cookie authCookie = new Cookie("authToken", "");
        authCookie.setMaxAge(0); // Delete the cookie
        authCookie.setPath("/"); // Make sure to match the path
        response.addCookie(authCookie);
        // Send response
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"success\",\"message\":\"Logged out successfully\"}");
    }
}