package com.app.controller.Test;

import com.erp.dao.implementation.user.ClientDAO;
import com.erp.dao.implementation.user.DelivererDAO;
import com.erp.dao.implementation.delivery.PackageDAO;
import com.erp.model.user.Client;
import com.erp.model.user.Deliverer;
import com.erp.model.delivery.Package;
import com.erp.model.enums.UserRole;
import com.erp.model.enums.VehicleType;
import com.erp.model.enums.PackageStatus;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.sql.Timestamp;

@WebServlet("/api/database/init")
public class TestInitServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // Initialize DAOs
        ClientDAO clientDAO = new ClientDAO();
        DelivererDAO delivererDAO = new DelivererDAO();
        PackageDAO packageDAO = new PackageDAO();

        // 1. Create Clients
        Client client1 = new Client(0, "alice@example.com", "alice_s", "hash1",
                "Alice", "Smith", "1234567890", UserRole.CLIENT,
                "123 Main St", "Metropolis", 12345, true);

        Client client2 = new Client(0, "bob@example.com", "bob_j", "hash2",
                "Bob", "Johnson", "0987654321", UserRole.CLIENT,
                "456 Elm St", "Gotham", 54321, false);
        // 2. Create Deliverers (Updated with: city, currentLoad, serialNumber, and rate)
        // Bike: Max 15kg, Current 0kg, Rate 5.0
        Deliverer deliverer1 = new Deliverer(0, "charlie@example.com", "charlie_b", "hash3",
                "Charlie", "Brown", "5555555555",true, "Metropolis", UserRole.DELIVERER,
                VehicleType.BIKE, true, 15.0, 0.0, "SN-BIKE-001", 5.0);

        // Car: Max 100kg, Current 0kg, Rate 15.0
        Deliverer deliverer2 = new Deliverer(0, "diana@example.com", "diana_p", "hash4",
                "Diana", "Prince", "4444444444",true, "Gotham", UserRole.DELIVERER,
                VehicleType.CAR, true, 100.0, 0.0, "SN-CAR-002", 15.0);

        // Truck: Max 500kg, Current 0kg, Rate 50.0
        Deliverer deliverer3 = new Deliverer(0, "ethan@example.com", "ethan_h", "hash5",
                "Ethan", "Hunt", "3333333333",true, "Star City", UserRole.DELIVERER,
                VehicleType.TRUCK, true, 500.0, 0.0, "SN-TRUCK-003", 50.0);

        // 3. Create Packages
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        Package package1 = new Package(0, 0, 0,
                VehicleType.BIKE, "123 Main St", "456 Elm St",
                5.0, 20.0, "10x10x10", "Books", "Fragile", 
                PackageStatus.CREATED, now);

        Package package2 = new Package(0, 0, 0,
                VehicleType.CAR, "789 Oak Ave", "321 Pine Rd",
                45.0, 60.0, "50x50x50", "Monitor", "Don't drop", 
                PackageStatus.CREATED, now);

        try {
            // Note: In your ClientDAO/DelivererDAO, the insert handles the User table too
            clientDAO.insert(client1);
            clientDAO.insert(client2);
            
            delivererDAO.insert(deliverer1);
            delivererDAO.insert(deliverer2);
            delivererDAO.insert(deliverer3);

            // Re-fetch client IDs if they were generated or set manually for the packages
            package1.setIdClientSource(client1.getId());
            package1.setIdClientDestination(client2.getId());
            
            package2.setIdClientSource(client2.getId());
            package2.setIdClientDestination(client1.getId());

            packageDAO.insert(package1);
            packageDAO.insert(package2);

            resp.getWriter().write("Database initialized with sample weight-management data.");
        } catch (Exception e) {
            System.err.println("Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}