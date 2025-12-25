package com.app.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import com.app.dao.Implementation.DeliveryPackageDAO;
import com.app.model.Deliverer;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.PackageStatus;
import com.app.service.DeliveryAssignmentService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


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
        out.println(json);
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