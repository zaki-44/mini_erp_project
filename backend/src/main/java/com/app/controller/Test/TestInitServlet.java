package com.app.controller.Test;

import com.erp.service.ClientService;
import com.erp.service.DelivererService;
import com.erp.service.PackageService;
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
    private ClientService clientService;
    private DelivererService delivererService;
    private PackageService packageService;
    
    @Override
    public void init() {
        clientService = new ClientService();
        delivererService = new DelivererService();
        packageService = new PackageService();
    }
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) {
        // 1. Create Clients
        Client client1 = new Client(0, "alice@example.com", "alice_s", "hash1",
                "Alice", "Smith", "1234567890", true, new Timestamp(System.currentTimeMillis()),
                "123 Main St", "Metropolis", 12345);

        Client client2 = new Client(0, "bob@example.com", "bob_j", "hash2",
                "Bob", "Johnson", "0987654321", true, new Timestamp(System.currentTimeMillis()),
                "456 Elm St", "Gotham", 54321);
        // 2. Create Deliverers (Updated with: city, currentLoad, serialNumber, and rate)
        // Bike: Max 15kg, Current 0kg, Rate 5.0
        Deliverer deliverer1 = new Deliverer(0, "charlie@example.com", "charlie_b", "hash3",
                "Charlie", "Brown", "5555555555", true, new Timestamp(System.currentTimeMillis()),
                VehicleType.BIKE, 15.0f, 0.0f, "SN-BIKE-001", "Metropolis", true, true);

        // Car: Max 100kg, Current 0kg, Rate 15.0
        Deliverer deliverer2 = new Deliverer(0, "diana@example.com", "diana_p", "hash4",
                "Diana", "Prince", "4444444444", true, new Timestamp(System.currentTimeMillis()),
                VehicleType.CAR, 100.0f, 0.0f, "SN-CAR-002", "Gotham", true, true);

        // Truck: Max 500kg, Current 0kg, Rate 50.0
        Deliverer deliverer3 = new Deliverer(0, "ethan@example.com", "ethan_h", "hash5",
                "Ethan", "Hunt", "3333333333", true, new Timestamp(System.currentTimeMillis()),
                VehicleType.TRUCK, 500.0f, 0.0f, "SN-TRUCK-003", "Star City", true, true);

        // 3. Create Packages
        Timestamp now = new Timestamp(System.currentTimeMillis());
        
        Package package1 = new Package(0, 0, 0,
                VehicleType.BIKE, "123 Main St", "456 Elm St",
                5.0f, 20.0f, "10x10x10", "Books", "Fragile", 
                PackageStatus.CREATED, now);

        Package package2 = new Package(0, 0, 0,
                VehicleType.CAR, "789 Oak Ave", "321 Pine Rd",
                45.0f, 60.0f, "50x50x50", "Monitor", "Don't drop", 
                PackageStatus.CREATED, now);

        try {
            // Register clients (with dummy verification codes since they're already verified)
            Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000);
            clientService.registerClient(client1, "DUMMY1", expiresAt);
            clientService.registerClient(client2, "DUMMY2", expiresAt);
            
            delivererService.registerDeliverer(deliverer1, "DUMMY3", expiresAt);
            delivererService.registerDeliverer(deliverer2, "DUMMY4", expiresAt);
            delivererService.registerDeliverer(deliverer3, "DUMMY5", expiresAt);

            // Re-fetch client IDs if they were generated or set manually for the packages
            package1.setClientSourceId(client1.getId());
            package1.setClientDestinationId(client2.getId());
            
            package2.setClientSourceId(client2.getId());
            package2.setClientDestinationId(client1.getId());

            packageService.createPackage(package1);
            packageService.createPackage(package2);

            resp.getWriter().write("Database initialized with sample weight-management data.");
        } catch (Exception e) {
            System.err.println("Initialization Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}