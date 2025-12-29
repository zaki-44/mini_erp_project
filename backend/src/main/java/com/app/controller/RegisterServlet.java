package com.app.controller;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;

import com.app.dao.implementation.*;
import com.app.model.*;
import com.app.model.Enums.Role;
import com.app.model.Enums.VehicleType;
import com.app.util.EmailService;
import com.app.util.JwtUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


@WebServlet("/signup")
public class RegisterServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        resp.setContentType("application/json");
        String firstname = jsonObject.get("firstname").getAsString();
        String lastname = jsonObject.get("lastname").getAsString();
        String username = jsonObject.get("username").getAsString();
        String email = jsonObject.get("email").getAsString();
        String password = jsonObject.get("password").getAsString();
        String phoneNumber = jsonObject.get("phonenumber").getAsString();
        Role role = Role.valueOf(jsonObject.get("role").getAsString().toUpperCase());
        User newUser = new User(0, email , username, password, firstname, lastname, phoneNumber, role , false);
        String code = JwtUtil.generateCode();
        PrintWriter out = resp.getWriter();
        UserDAO userDAO = new UserDAO();
        try{
            if(userDAO.emailExists(email)){
                        out.println("{\"status\": \"fail\", \"message\": \"Email already in use\"}");
                        return;
            }
            if(userDAO.usernameExists(username)){
                        out.println("{\"status\": \"fail\", \"message\": \"Username already in use\"}");
                        return;
            }
        }
        catch(Exception e){
            System.out.println("Error checking existing user: " + e.getMessage());
            out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
            return;
        }
        if(role == Role.CLIENT){
            String address = jsonObject.get("address").getAsString();
            String city = jsonObject.get("city").getAsString();
            int postalCode = jsonObject.get("postalcode").getAsInt();
            ClientDAO clientDAO = new ClientDAO();
            Client newClient = new Client(newUser, address, city, postalCode);
            try{
                
                clientDAO.insert(newClient);
                EmailVerificationDAO verificationDAO = new EmailVerificationDAO();
                verificationDAO.saveVerificationCode(newClient.getId(), code);
                EmailService.sendEmail(newClient.getEmail() , "Verification Email" , "Your verification code is : " + code);
                out.println("{\"status\": \"success\", \"message\": \"Client registered successfully\"}");
            }
            catch(Exception e){
                System.out.println("Error registering client: " + e.getMessage());
                out.println("{\"status\": \"fail\"}");
            }
        }
        else if(role == Role.DELIVERER){
            VehicleType vehicleType = VehicleType.valueOf(jsonObject.get("vehicletype").getAsString().toUpperCase());
            String city = jsonObject.get("city").getAsString();
            double maxWeight = jsonObject.get("maxweight").getAsDouble();
            String serialNumber = jsonObject.get("serialnumber").getAsString();
            DelivererDAO delivererDAO = new DelivererDAO();
            Deliverer newDeliverer = new Deliverer(newUser, city, role, vehicleType, true, maxWeight, 0.0, serialNumber, 0.0);
            try{
                delivererDAO.insert(newDeliverer);
                EmailVerificationDAO verificationDAO = new EmailVerificationDAO();
                verificationDAO.saveVerificationCode(newDeliverer.getId(), code);
                EmailService.sendEmail(newDeliverer.getEmail() , "Verification Email" , "Your verification code is : " + code);
                out.println("{\"status\": \"success\", \"message\": \"Deliverer registered successfully\"}");
            }
            catch(Exception e){
                System.out.println("Error inserting deliverer: " + e.getMessage());
                out.println("{\"status\": \"fail\"}");
            }
        }
        
    }
}
