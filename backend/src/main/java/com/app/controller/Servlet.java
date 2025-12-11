package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.SQLException;
import java.util.Map;
import com.google.gson.Gson;


import com.app.dao.Implementation.UserDAO;
import com.app.model.User;


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
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        Map<String, String> body = gson.fromJson(reader, Map.class);
        int id = body.get("id") != null ? Integer.parseInt(body.get("id")) : 0;
        System.out.println("Received ID: " + id);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        UserDAO userDAO = new UserDAO();
        try{
            User user = userDAO.findById(id);
            String userJson = gson.toJson(user);
            out.println(userJson);
        }
        catch(SQLException e){
            e.printStackTrace();
        }

    }
}