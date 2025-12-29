package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import com.google.gson.Gson;
import com.app.dao.implementation.DeliveryPackageDAO;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.PackageStatus;

@WebServlet("/api/packages/*")
public class DeliveryPackageServlet extends HttpServlet {

    private DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();
    private Gson gson = new Gson();

    /**
     * GET /api/packages - Get all packages
     * GET /api/packages/{id} - Get package by ID
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                // Get all packages
                List<DeliveryPackage> packages = packageDAO.findAll();
                String json = gson.toJson(packages);
                resp.setStatus(HttpServletResponse.SC_OK);
                out.print(json);
            } else {
                // Get package by ID
                String[] splits = pathInfo.split("/");
                if (splits.length > 1) {
                    int id = Integer.parseInt(splits[1]);
                    DeliveryPackage pkg = packageDAO.findById(id);

                    if (pkg != null) {
                        String json = gson.toJson(pkg);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.print(json);
                    } else {
                        resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                        out.print("{\"error\": \"Package not found\"}");
                    }
                }
            }
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid ID format\"}");
        }
    }

    /**
     * POST /api/deliveries - Create new delivery
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            // Read JSON from request body
            BufferedReader reader = req.getReader();
            DeliveryPackage pkg = gson.fromJson(reader, DeliveryPackage.class);

            if (pkg == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Request body is empty or invalid JSON\"}");
                return;
            }

            // Set default values
            if (pkg.getStatus() == null) {
                pkg.setStatus(PackageStatus.CREATED);
            }
            if (pkg.getCreatedAt() == null) {
                pkg.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }

            // Insert into database
            packageDAO.insert(pkg);
            // Return created package
            String json = gson.toJson(pkg);
            resp.setStatus(HttpServletResponse.SC_CREATED);
            out.print(json);

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid request data: " + e.getMessage() + "\"}");
            e.printStackTrace();
        }
    }

    /**
     * PUT /api/packages/{id} - Update package
     */
    @Override
protected void doPut(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {
    resp.setContentType("application/json");
    resp.setCharacterEncoding("UTF-8");
    PrintWriter out = resp.getWriter();

    try {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Package ID is required\"}");
            return;
        }

        String[] splits = pathInfo.split("/");
        int id = Integer.parseInt(splits[1]);

        // 1. FETCH EXISTING DATA FIRST (Crucial Step!)
        DeliveryPackage existing = packageDAO.findById(id);
        
        if (existing == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.print("{\"error\": \"Package not found\"}");
            return;
        }

        // 2. Read new updates
        BufferedReader reader = req.getReader();
        DeliveryPackage updates = gson.fromJson(reader, DeliveryPackage.class);

        // 3. UPDATE ONLY WHAT IS NOT NULL
        // This prevents overwriting existing data with nulls
        if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
        if (updates.getVehicleTypeNeeded() != null) existing.setVehicleTypeNeeded(updates.getVehicleTypeNeeded());
        if (updates.getAddressSource() != null) existing.setAddressSource(updates.getAddressSource());
        if (updates.getAddressDestination() != null) existing.setAddressDestination(updates.getAddressDestination());
        if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
        if (updates.getWeight() > 0) existing.setWeight(updates.getWeight());
        if (updates.getPrice() > 0) existing.setPrice(updates.getPrice());

        // 4. SAVE THE MERGED OBJECT
        packageDAO.update(existing);

        String json = gson.toJson(existing);
        resp.setStatus(HttpServletResponse.SC_OK);
        out.print(json);

    } catch (SQLException e) {
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.print("{\"error\": \"" + e.getMessage() + "\"}");
        e.printStackTrace();
    } catch (NumberFormatException e) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\": \"Invalid ID format\"}");
    } catch (Exception e) {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        out.print("{\"error\": \"Invalid request data: " + e.getMessage() + "\"}");
        e.printStackTrace();
    }
}

    /**
     * DELETE /api/packages/{id} - Delete package
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();

        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Delivery ID is required\"}");
                return;
            }

            String[] splits = pathInfo.split("/");
            int id = Integer.parseInt(splits[1]);

            // Check if package exists
            DeliveryPackage existing = packageDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Package not found\"}");
                return;
            }

            // Delete from database
            packageDAO.delete(id);

            resp.setStatus(HttpServletResponse.SC_OK);
            out.print("{\"message\": \"Package deleted successfully\"}");

        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"" + e.getMessage() + "\"}");
            e.printStackTrace();
        } catch (NumberFormatException e) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid ID format\"}");
        }
    }
}