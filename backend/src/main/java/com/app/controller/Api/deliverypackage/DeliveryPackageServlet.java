package com.app.controller.Api.deliverypackage;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import com.google.gson.Gson;
import com.app.dao.Implementation.DeliveryPackageDAO;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.PackageStatus;

@WebServlet("/api/packages/*")
public class DeliveryPackageServlet extends HttpServlet {

    private DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();
    private Gson gson = new Gson();

    /**
     * GET /api/packages - Get all packages
     * GET /api/packages/{id} - Get package by ID
     * GET /api/packages?idDeliverer={delivererId} - Get packages assigned to a deliverer 
     * GET /api/packages?idClient={clientId} - Get packages created by a client 
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Access denied.\"}");
            return;
        }
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                
                String delivererIdParam = req.getParameter("idDeliverer");
                String clientIdParam = req.getParameter("idClient");

                if (delivererIdParam != null) {
                    if(!role.equals("ADMIN")){
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Admins only.\"}");
                        return;
                    }
                    int delivererId = Integer.parseInt(delivererIdParam);
                    List<DeliveryPackage> packages = packageDAO.findByDelivererId(delivererId);
                    String json = gson.toJson(packages);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.print(json);
                } else if (clientIdParam != null) {
                    if(!role.equals("ADMIN")){
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Admins only.\"}");
                        return;
                    }
                    int clientId = Integer.parseInt(clientIdParam);
                    List<DeliveryPackage> packages = packageDAO.findByClientId(clientId);
                    String json = gson.toJson(packages);
                    resp.setStatus(HttpServletResponse.SC_OK);
                    out.print(json);
                } else {
                    if(role.equals("CLIENT")){
                        int clientId = userId;
                        List<DeliveryPackage> packages = packageDAO.findByClientId(clientId);
                        String json = gson.toJson(packages);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.print(json);
                        return;
                    }
                    else if(role.equals("ADMIN")){
                        List<DeliveryPackage> packages = packageDAO.findAll();
                        String json = gson.toJson(packages);
                        resp.setStatus(HttpServletResponse.SC_OK);
                        out.print(json);
                        return;
                    }
                }
            } else {
                
                String[] splits = pathInfo.split("/");
                if (splits.length > 1) {
                    int id = Integer.parseInt(splits[1]);
                    DeliveryPackage pkg = packageDAO.findById(id);
                    if(!role.equals("ADMIN") || !packageDAO.ownsPackage(userId , id)){
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"You can only access your own packages.\"}");
                        return;
                    }
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
     * POST /api/packages - Create new package
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null || !role.equals("CLIENT")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Only clients can create packages\"}");
            return;
        }
        int clientId = userId;
        try {
            BufferedReader reader = req.getReader();
            DeliveryPackage pkg = gson.fromJson(reader, DeliveryPackage.class);

            if (pkg == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Request body is empty or invalid JSON\"}");
                return;
            }

            if (pkg.getStatus() == null) {
                pkg.setStatus(PackageStatus.CREATED);
            }
            if (pkg.getCreatedAt() == null) {
                pkg.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            }

            pkg.setIdClientSource(clientId);
            packageDAO.insert(pkg);

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

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null || !role.equals("CLIENT")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Only clients can update packages\"}");
            return;
        }
        int clientId = userId;

        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Package ID is required\"}");
                return;
            }

            String[] splits = pathInfo.split("/");
            int id = Integer.parseInt(splits[1]);

            DeliveryPackage existing = packageDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Package not found\"}");
                return;
            }
            if (existing.getIdClientSource() != clientId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"You do not own this package.\"}");
                return;
            }

            BufferedReader reader = req.getReader();
            DeliveryPackage updates = gson.fromJson(reader, DeliveryPackage.class);

            if (updates.getStatus() != null) existing.setStatus(updates.getStatus());
            if (updates.getVehicleTypeNeeded() != null) existing.setVehicleTypeNeeded(updates.getVehicleTypeNeeded());
            if (updates.getAddressSource() != null) existing.setAddressSource(updates.getAddressSource());
            if (updates.getAddressDestination() != null) existing.setAddressDestination(updates.getAddressDestination());
            if (updates.getDescription() != null) existing.setDescription(updates.getDescription());
            if (updates.getWeight() > 0) existing.setWeight(updates.getWeight());
            if (updates.getPrice() > 0) existing.setPrice(updates.getPrice());

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
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");
        if (userId == null || role == null || !role.equals("CLIENT")) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Only clients can delete packages\"}");
            return;
        }
        int clientId = userId;
        try {
            String pathInfo = req.getPathInfo();

            if (pathInfo == null || pathInfo.equals("/")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                out.print("{\"error\": \"Package ID is required\"}");
                return;
            }

            String[] splits = pathInfo.split("/");
            int id = Integer.parseInt(splits[1]);

            DeliveryPackage existing = packageDAO.findById(id);
            if (existing == null) {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                out.print("{\"error\": \"Package not found\"}");
                return;
            }
            if (existing.getIdClientSource() != clientId) {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"You do not own this package.\"}");
                return;
            }

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