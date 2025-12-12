package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;
import com.google.gson.Gson;
import com.app.model.Enums.AffectationStatus;
import com.app.model.Enums.NotificationType;
import com.app.model.Enums.PackageStatus;
import com.app.model.Enums.VehicleType;
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
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        try {
            Gson gson = new Gson();

            // Create DeliveryPackageDAO instance
            DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();

            // Create a new DeliveryPackage
            DeliveryPackage pkg = new DeliveryPackage();
            pkg.setIdClientSource(17);        // Make sure client with ID 17 exists
            pkg.setIdClientDestination(18);   // Make sure client with ID 18 exists
            pkg.setVehicleTypeNeeded(VehicleType.CAR);
            pkg.setAddressSource("123 Main St");
            pkg.setAddressDestination("456 Elm St");
            pkg.setWeight(5.5f);
            pkg.setPrice(25.0f);
            pkg.setDimensions("30x20x15");
            pkg.setDescription("Test package");
            pkg.setStatus(PackageStatus.CREATED);
            pkg.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

            // Print to console before inserting
            pkg.print();

            // Insert into database
            packageDAO.insert(pkg);

            // Print after insert (ID should be set)
            System.out.println("After insert:");
            pkg.print();

            // Update the package
            pkg.setStatus(PackageStatus.ASSIGNED);
            packageDAO.update(pkg);
            System.out.println("After update:");
            pkg.print();

            // Retrieve all packages
            List<DeliveryPackage> packages = packageDAO.findAll();

            // Convert list to JSON
            String json = gson.toJson(packages);
            out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}