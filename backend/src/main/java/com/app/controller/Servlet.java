package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;


import com.app.dao.Implementation.*;
import com.app.model.*;


@WebServlet("/api/database/test")
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String message = "Do post";
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        out.println("{\"message\": \"" +"From server :" +message + "\"}");
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DelivererDAO delivererDAO = new DelivererDAO();
        Deliverer deliverer = null;
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        try {
            List<Deliverer> deliverers = delivererDAO.findAll();
            for(Deliverer d : deliverers){
                d.print();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}