package com.app.controller;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import com.app.util.EmailService;

@WebServlet("/api/email/send")
public class TestServletEmail extends HttpServlet{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/plain");
        PrintWriter out = resp.getWriter();
        String to = req.getParameter("to");
        String subject = req.getParameter("subject");
        String body = req.getParameter("body");

        try {
            EmailService.sendEmail(to, subject, body);
            out.println("Email sent successfully to: " + to);
        } catch (Exception e) {
            out.println("Error sending email: " + e.getMessage());
        }
    }
}