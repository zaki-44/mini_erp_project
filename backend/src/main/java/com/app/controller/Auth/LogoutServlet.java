package com.app.controller.Auth;


import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Cookie authCookie = new Cookie("authToken", "");
        authCookie.setMaxAge(0);
        authCookie.setPath("/"); 
        response.addCookie(authCookie);
        
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"status\":\"success\",\"message\":\"Logged out successfully\"}");
    }
}