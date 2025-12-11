package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;
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
        UserDAO userDAO = new UserDAO();
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        try {
            userDAO.delete(2);
            List<User> users = userDAO.findAll();
            String usersJson = new Gson().toJson(users);
            out.println(usersJson);            
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}