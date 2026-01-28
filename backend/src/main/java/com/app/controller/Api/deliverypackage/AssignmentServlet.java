package com.app.controller.Api.deliverypackage;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.app.dao.Implementation.AffectationDAO;
import com.app.dao.Implementation.DelivererDAO;
import com.app.dao.Implementation.DeliveryPackageDAO;
import com.app.model.Affectation;
import com.app.model.Deliverer;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.AffectationStatus;
import com.app.model.Enums.PackageStatus;
import com.app.service.DeliveryAssignmentService;

@WebServlet("/api/assignments/*")
public class AssignmentServlet extends HttpServlet {

    private DeliveryAssignmentService assignmentService;
    private AffectationDAO affectationDAO;
    private DeliveryPackageDAO packageDAO;
    private DelivererDAO delivererDAO;
    private Gson gson;

    @Override
    public void init() {
        assignmentService = new DeliveryAssignmentService();
        affectationDAO = new AffectationDAO();
        packageDAO = new DeliveryPackageDAO();
        delivererDAO = new DelivererDAO();
        gson = new Gson();
    }

    /**
     * GET /api/assignments 
     * - Returns the list of assignments for the logged-in Deliverer or Client.
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");

        if (userId == null || role == null) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            out.print("{\"error\": \"Access denied. Deliverers only.\"}");
            return;
        }

        try {
            List<Affectation> myJobs;
            if (role.equals("DELIVERER")) {
                myJobs = affectationDAO.findByDelivererId(userId);
            } else if (role.equals("CLIENT")) {
                myJobs = affectationDAO.findByClientId(userId);
            } else {
                resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                out.print("{\"error\": \"Access denied. Deliverers or Clients only.\"}");
                return;
            }
            out.print(gson.toJson(myJobs));
        } catch (SQLException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Database error: " + e.getMessage() + "\"}");
        }
    }

    /**
     * POST /api/assignments/{action}/{id}
     * * Actions for CLIENT:
     * - /request/{packageId}
     * * Actions for DELIVERER:
     * - /accept/{affectationId}
     * - /reject/{affectationId}
     * - /complete/{affectationId}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();

        // 1. Validate Auth
        Integer userId = (Integer) req.getAttribute("userId");
        String role = (String) req.getAttribute("userRole");

        if (userId == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"Unauthorized\"}");
            return;
        }

        // 2. Parse URL
        String pathInfo = req.getPathInfo(); 
        if (pathInfo == null || pathInfo.split("/").length < 3) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"Invalid endpoint format. Use /action/id\"}");
            return;
        }

        String[] parts = pathInfo.split("/");
        String action = parts[1];
        int id = Integer.parseInt(parts[2]);

        try {
            switch (action) {
                case "request":
                    if (!role.equals("CLIENT")) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Only clients can request drivers\"}");
                        return;
                    }
                    handleRequestDriver(id, out);
                    break;

                case "accept":
                    if (!role.equals("DELIVERER")) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Only deliverers can accept jobs\"}");
                        return;
                    }
                    handleAccept(id, userId, out);
                    break;

                case "reject":
                    if (!role.equals("DELIVERER")) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Only deliverers can reject jobs\"}");
                        return;
                    }
                    handleReject(id, userId, out);
                    break;

                case "complete":
                    if (!role.equals("DELIVERER")) {
                        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
                        out.print("{\"error\": \"Only deliverers can complete jobs\"}");
                        return;
                    }
                    handleComplete(id, userId, out);
                    break;

                default:
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    out.print("{\"error\": \"Unknown action: " + action + "\"}");
            }
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"Server error: " + e.getMessage() + "\"}");
        }
    }

    private void handleRequestDriver(int packageId, PrintWriter out) {
        Deliverer match = assignmentService.autoAssignPackage(packageId);

        JsonObject json = new JsonObject();
        if (match != null) {
            json.addProperty("status", "success");
            json.addProperty("message", "Driver found and assigned");
            json.addProperty("delivererId", match.getId());
            json.addProperty("delivererName", match.getFirstName() + " " + match.getLastName());
        } else {
            json.addProperty("status", "pending");
            json.addProperty("message", "No suitable driver found at this moment. Status remains CREATED.");
        }
        out.print(gson.toJson(json));
    }

    private void handleAccept(int affectationId, int delivererId, PrintWriter out) throws SQLException {
        Affectation aff = affectationDAO.findById(affectationId);

        if (aff == null || aff.getIdDeliverer() != delivererId) {
            out.print("{\"error\": \"Assignment not found or not yours\"}");
            return;
        }

        if (aff.getStatus() == AffectationStatus.PENDING) {
            aff.setStatus(AffectationStatus.ACCEPTED);
            aff.setAssignedAt(new Timestamp(System.currentTimeMillis()));
            affectationDAO.update(aff);

            DeliveryPackage pkg = packageDAO.findById(aff.getIdPackage());
            pkg.setStatus(PackageStatus.PICKEDUP); 
            packageDAO.update(pkg);

            out.print("{\"message\": \"Assignment Accepted\"}");
        } else {
            out.print("{\"error\": \"Cannot accept. Current status: " + aff.getStatus() + "\"}");
        }
    }

    private void handleReject(int affectationId, int delivererId, PrintWriter out) throws SQLException {
        Affectation aff = affectationDAO.findById(affectationId);

        if (aff == null || aff.getIdDeliverer() != delivererId) {
            out.print("{\"error\": \"Assignment not found or not yours\"}");
            return;
        }

        if (aff.getStatus() == AffectationStatus.PENDING) {
            aff.setStatus(AffectationStatus.REJECTED);
            affectationDAO.update(aff);

            DeliveryPackage pkg = packageDAO.findById(aff.getIdPackage());
            pkg.setStatus(PackageStatus.CREATED);
            packageDAO.update(pkg);

            revertDelivererLoad(delivererId, pkg.getWeight());

            out.print("{\"message\": \"Assignment Rejected\"}");
        } else {
            out.print("{\"error\": \"Cannot reject. Current status: " + aff.getStatus() + "\"}");
        }
    }

    private void handleComplete(int affectationId, int delivererId, PrintWriter out) throws SQLException {
        Affectation aff = affectationDAO.findById(affectationId);

        if (aff == null || aff.getIdDeliverer() != delivererId) {
            out.print("{\"error\": \"Assignment not found or not yours\"}");
            return;
        }

        if (aff.getStatus() == AffectationStatus.ACCEPTED || aff.getStatus() == AffectationStatus.ONROUTE) {
            aff.setStatus(AffectationStatus.COMPLETED);
            affectationDAO.update(aff);

            DeliveryPackage pkg = packageDAO.findById(aff.getIdPackage());
            pkg.setStatus(PackageStatus.DELIVERED);
            packageDAO.update(pkg);

            revertDelivererLoad(delivererId, pkg.getWeight());

            out.print("{\"message\": \"Delivery Completed!\"}");
        } else {
            out.print("{\"error\": \"Cannot complete. Current status: " + aff.getStatus() + "\"}");
        }
    }

    private void revertDelivererLoad(int delivererId, double weightToFree) throws SQLException {
        Deliverer d = delivererDAO.findById(delivererId);
        double newLoad = d.getCurrentLoad() - weightToFree;
        if (newLoad < 0) newLoad = 0;

        d.setCurrentLoad(newLoad);
        d.setAvailable(true);
        delivererDAO.update(d);
    }
}