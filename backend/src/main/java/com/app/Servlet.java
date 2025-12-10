package com.app;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.Map;

import com.google.gson.Gson;


@WebServlet("/api/hello")
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter("message");
        System.out.println("Received message: " + message);
        if (message == null || message.isEmpty()) {
            message = "Hello, World!";
        }
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println("{\"message\": \"" +"From server :" +message + "\"}");
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        Map<String, String> body = gson.fromJson(reader, Map.class);
        String message = body.get("message");
        System.out.println("Received message: " + message);
        if (message == null || message.isEmpty()) {
            message = "Hello, World!";
        }
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println("{\"message\": \"" +"From server :" +message.toUpperCase() + "\"}");
    }
}