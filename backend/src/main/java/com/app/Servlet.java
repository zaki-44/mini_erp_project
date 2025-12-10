package com.app;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;


@WebServlet("/api/hello")
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = req.getParameter("message");
        if (message == null || message.isEmpty()) {
            message = "Hello, World!";
        }
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println("{\"message\": \"" +"From server :" +message + "\"}");
        out.flush();
    }
}