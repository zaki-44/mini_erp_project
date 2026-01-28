package com.app.controller.Api.admin;

import com.app.dao.Implementation.DelivererDAO;
import com.app.dao.Implementation.UserDAO;
import com.app.model.Deliverer;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@WebServlet("/api/admin/*")
public class AdminServlet extends HttpServlet {

    private UserDAO userDAO;
    private DelivererDAO delivererDAO;
    private Gson gson;

    public void init() {
        userDAO = new UserDAO(); 
        delivererDAO = new DelivererDAO();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String pathInfo = req.getPathInfo();
        String role = (String) req.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\": \"Forbidden\"}");
            return;
        }
        // Route: GET /api/admin/pending-deliverers
        if ("/pending-deliverers".equals(pathInfo)) {
            try{
                List<Deliverer> pending = delivererDAO.getPendingDeliverers();
                resp.setContentType("application/json");
                resp.getWriter().write(gson.toJson(pending));
            }
            catch(Exception e){
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"error\": \"Server error\"}");
            }
        } else {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        String role = (String) req.getAttribute("userRole");
        if (role == null || !role.equals("ADMIN")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.getWriter().write("{\"error\": \"Forbidden\"}");
            return;
        }
        // Route: POST /api/admin/approve?id=5
        if ("/approve".equals(pathInfo)) {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                try {
                    int userId = Integer.parseInt(idParam);
                    boolean success = userDAO.approveDeliverer(userId);
                    
                    if (success) {
                        resp.getWriter().write("{\"message\": \"Deliverer Approved Successfully\"}");
                    } else {
                        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        resp.getWriter().write("{\"error\": \"Failed to approve\"}");
                    }
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Missing ID parameter\"}");
            }
        }
    }
    
}