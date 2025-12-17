package com.app.controller;

import com.app.dao.Implementation.*;
import com.app.model.*;
import com.app.model.Enums.*;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

@WebServlet("/api/database/init")
public class InitServlet extends  HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        //Create the deliverers and Clients 
        ClientDAO clientDAO = new ClientDAO();
        DelivererDAO delivererDAO = new DelivererDAO();
        DeliveryPackageDAO packageDAO = new DeliveryPackageDAO();
        Client client1 = new Client(0, "client1@example.com", "client1", "hashedpassword1",
                "Alice", "Smith", "1234567890", Role.CLIENT,
                "123 Main St", "Metropolis", 12345, true, true);
        Client client2 = new Client(0, "client2@example.com", "client2", "hashedpassword2",
                "Bob", "Johnson", "0987654321", Role.CLIENT,
                "456 Elm St", "Gotham", 54321, false, true);
        Deliverer deliverer1 = new Deliverer(0, "deliverer1@example.com", "deliverer1", "hashedpassword3",
                "Charlie", "Brown", "5555555555", "Metropolis", Role.DELIVERER,
                VehicleType.BIKE, true, 15.0);
        Deliverer deliverer2 = new Deliverer(0, "deliverer2@example.com", "deliverer2", "hashedpassword4",
                "Diana", "Prince", "4444444444", "Gotham", Role.DELIVERER,
                VehicleType.CAR, true, 100.0);
        Deliverer deliverer3 = new Deliverer(0, "deliverer3@example.com", "deliverer3", "hashedpassword5",
                "Ethan", "Hunt", "3333333333", "Star City", Role.DELIVERER,
                VehicleType.TRUCK, false, 500.0);
        DeliveryPackage package1 = new DeliveryPackage(0, 1, 2,
                VehicleType.BIKE, "123 Main St, Metropolis", "456 Elm St, Gotham",
                5.0, 20.0, "10x10x10 cm", "Books", "Leave at front door", PackageStatus.CREATED, null);
        
 
        
        try {
                clientDAO.insert(client1);
                clientDAO.insert(client2);
                delivererDAO.insert(deliverer1);
                delivererDAO.insert(deliverer2);
                delivererDAO.insert(deliverer3);
                packageDAO.insert(package1);

        } catch (Exception e) {
            System.out.println("Error inserting client1: " + e.getMessage());
            e.printStackTrace();
        }


    }
}
