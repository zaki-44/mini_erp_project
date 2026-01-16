package com.app.controller.Api.admin;

import com.erp.service.DelivererService;
import com.erp.model.user.Deliverer;
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

    private DelivererService delivererService;
    private Gson gson;

    public void init() {
        delivererService = new DelivererService();
        gson = new Gson();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        setAccessControlHeaders(resp);

        String pathInfo = req.getPathInfo();

        // Route: GET /api/admin/pending-deliverers
        if ("/pending-deliverers".equals(pathInfo)) {
            try{
                // Get all deliverers and filter for those not approved
                List<Deliverer> all = delivererService.getAllDeliverers();
                List<Deliverer> pending = all.stream()
                    .filter(d -> !d.isApproved())
                    .collect(java.util.stream.Collectors.toList());
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
        setAccessControlHeaders(resp);

        String pathInfo = req.getPathInfo();

        // Route: POST /api/admin/approve?id=5
        if ("/approve".equals(pathInfo)) {
            String idParam = req.getParameter("id");
            if (idParam != null) {
                try {
                    int userId = Integer.parseInt(idParam);
                    // Approve the deliverer using the service
                    delivererService.approveDeliverer(userId);
                    resp.getWriter().write("{\"message\": \"Deliverer Approved Successfully\"}");
                } catch (NumberFormatException e) {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().write("{\"error\": \"Invalid ID format\"}");
                } catch (Exception e) {
                    resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    resp.getWriter().write("{\"error\": \"Server error\"}");
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().write("{\"error\": \"Missing ID parameter\"}");
            }
        }
    }
    
    // CORS Helper
    private void setAccessControlHeaders(HttpServletResponse resp) {
        resp.setHeader("Access-Control-Allow-Origin", "http://localhost:5173");
        resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
    }
}