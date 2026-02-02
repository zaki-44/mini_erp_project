package com.app.controller.Api;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;

import com.google.gson.Gson;
import com.app.service.NotificationService;
import com.app.model.Notification;

@WebServlet("/api/notifications")
public class NotificationServlet extends HttpServlet {

    private NotificationService notificationService;
    private Gson gson;

    @Override
    public void init() {
        notificationService = new NotificationService();
        gson = new Gson();
    }
    // GET /api/notifications - Get notifications for the authenticated user
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        try {
            List<Notification> notifications = notificationService.getNotificationsForUser(userId);
            out.print(gson.toJson(notifications));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to retrieve notifications\"}");
        }

    }
    //POST : mark notification as read
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        int notificationId = Integer.parseInt(req.getParameter("notificationId"));
        try {
            notificationService.markAsRead(notificationId, userId);
            out.print("{\"status\": \"success\", \"message\": \"Notification marked as read\"}");
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to process notification\"}");
        }
    }
}