package com.app.controller.Api.client;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;
import com.app.model.Client;

import com.google.gson.Gson;
import com.app.service.SearchService;

@WebServlet("/api/search")
public class SearchServlet extends HttpServlet {

    private SearchService searchService;
    private Gson gson;

    @Override
    public void init() {
        searchService = new SearchService();
        gson = new Gson();
    }
    // GET /api/search - Get search results for the authenticated user
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
            String query = req.getParameter("q");
            List<Client> results = searchService.searchUsersByName(query);
            out.print(gson.toJson(results));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Failed to retrieve search results\"}");
        }

    }
}