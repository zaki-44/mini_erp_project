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
        Client client1 = new Client(0, "client1", "client1", "hashed_password_here",
                "Alice", "Smith", "1112223333","Blida", Role.CLIENT,
                "123 Client St", true);
        Client client2 = new Client(0, "client2", "client2", "hashed_password_here",
                "Bob", "Johnson", "4445556666","Blida" ,Role.CLIENT,
                "456 Client Ave", true);
        Deliverer deliverer1 = new Deliverer(0, "deliverer1", "deliverer1", "hashed_password_here",
                "Charlie", "Brown", "7778889999","Algiers" , Role.DELIVERER,
                VehicleType.BIKE, true, 15.0);
        Deliverer deliverer2 = new Deliverer(0, "deliverer2", "deliverer2", "hashed_password_here",
                "Diana", "Prince", "0001112222","Blida" , Role.DELIVERER,
                VehicleType.CAR, true, 50.0);
        DeliveryPackage pkg = new DeliveryPackage(0, 1, 2,
                VehicleType.CAR, "123 Main St", "456 Elm St",
                10.0, 30.0, "30x20x15", "Sample package", PackageStatus.CREATED,
                new java.sql.Timestamp(System.currentTimeMillis())
        );
        
 
        
        try {
            clientDAO.insert(client2);
            clientDAO.insert(client1);
            delivererDAO.insert(deliverer1);
            delivererDAO.insert(deliverer2);
            packageDAO.insert(pkg);

        } catch (Exception e) {
            System.out.println("Error inserting client1: " + e.getMessage());
            e.printStackTrace();
        }


    }
}
