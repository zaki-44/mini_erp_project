package com.app.controller.Api.client;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.app.service.RateService;
import com.app.model.Rate;
/*
    GET /api/rates?delivererId={id} - Get average rating for a deliverer
    GET /api/rates/{id} - Get specific rate by ID
    POST /api/rates - Submit a new rating
    PUT /api/rates - Update an existing rating
    DELETE /api/rates/{id} - Delete a rating by ID
*/
@WebServlet("/api/rates/*")
public class RateServlet extends HttpServlet {

    private RateService rateService;
    private Gson gson;

    @Override
    public void init() {
        rateService = new RateService();
        gson = new Gson();
    }

    // GET /api/rates?delivererId={id} - Get average rating for a deliverer
    // GET /api/rates/{id} - Get specific rate by ID
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        String pathInfo = req.getPathInfo();
        String delivererIdParam = req.getParameter("delivererId");

        try {
            if (delivererIdParam != null) {
                int delivererId = Integer.parseInt(delivererIdParam);
                double average = rateService.calculateAverageRating(delivererId);
                
                JsonObject json = new JsonObject();
                json.addProperty("delivererId", delivererId);
                json.addProperty("averageRating", average);
                out.print(gson.toJson(json));

            } else if (pathInfo != null && pathInfo.length() > 1) {
                String[] splits = pathInfo.split("/");
                if (splits.length > 1) {
                    int rateId = Integer.parseInt(splits[1]);
                    Rate rate = rateService.getRateById(rateId);
                    
                    if (rate != null) {
                        out.print(gson.toJson(rate));
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Rate not found\"}");
                    }
                }
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Missing parameters\"}");
            }
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
    // POST /api/rates - Submit a new rating
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String role = (String) req.getAttribute("userRole");
        if(role == null || !role.equals("CLIENT")){
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Forbidden only for clients\"}");
            return;
        }
        Integer userId = (Integer) req.getAttribute("userId");
        if(userId == null){
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        int idClient = userId;
        try {
            BufferedReader reader = req.getReader();
            Rate rate = gson.fromJson(reader, Rate.class);
            rate.setIdClient(idClient); 
            if (rate == null || rate.getRating() < 1 || rate.getRating() > 5) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Invalid rating value\"}");
                return;
            }

            rateService.rateDeliverer(
                rate.getIdDeliverer(),
                rate.getIdClient(),
                rate.getRating(),
                rate.getComment()
            );

            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print("{\"message\": \"Rating submitted successfully\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to submit rating\"}");
            e.printStackTrace();
        }
    }
    // PUT /api/rates - Update an existing rating
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String role = (String) req.getAttribute("userRole");
        if(role == null || !role.equals("CLIENT")){
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Forbidden only for clients\"}");
            return;
        }
        try {
            BufferedReader reader = req.getReader();
            Rate rate = gson.fromJson(reader, Rate.class);
            Integer userId = (Integer) req.getAttribute("userId");
            if(userId == null){
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                out.print("{\"error\": \"Unauthorized\"}");
                return;
            }
            int idClient = userId;
            rate.setIdClient(idClient);

            rateService.updateRate(
                rate.getIdDeliverer(),
                rate.getIdClient(),
                rate.getRating(),
                rate.getComment()
            );

            out.print("{\"message\": \"Rating updated successfully\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to update rating\"}");
        }
    }
    // DELETE /api/rates/{id} - Delete a rating by ID
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String role = (String) req.getAttribute("userRole");
        if(role == null || !role.equals("CLIENT")){
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Forbidden only for clients\"}");
            return;
        }
        Integer userId = (Integer) req.getAttribute("userId");
        if(userId == null){
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }
        int idClient = userId;
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Rate ID is required\"}");
                return;
            }

            int id = Integer.parseInt(pathInfo.split("/")[1]);
            rateService.deleteRate(id , idClient);

            out.print("{\"message\": \"Rating deleted successfully\"}");

        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to delete rating\"}");
        }
    }
}