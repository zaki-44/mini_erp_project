package com.app.controller;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.util.List;
import com.google.gson.Gson;
import com.app.model.Enums.PackageStatus;
import com.app.service.DeliveryAssignmentService;
import com.app.dao.Implementation.*;
import com.app.model.*;


@WebServlet("/api/database/test")
public class Servlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        DeliveryAssignmentService assignmentService = new DeliveryAssignmentService();
        Integer id = Integer.parseInt(req.getParameter("id"));
        DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();
        Deliverer deliverer = assignmentService.autoAssignPackage(id);
        DeliveryPackage pkg = null;
        try{
            pkg = packageDAO.findById(id);
        }
        catch(Exception e){
            System.out.println("Error fetching package: " + e.getMessage());
        }
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        Gson gson = new Gson();
        String json = gson.toJson(deliverer);
        String pkgJson = gson.toJson(pkg);
        out.println("Deliverer assigned:");
        out.println(json);
        out.println("Package after assignment:");
        out.println(pkgJson);
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
            DeliveryPackage pkg = packageDAO.findAll().get(0);

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