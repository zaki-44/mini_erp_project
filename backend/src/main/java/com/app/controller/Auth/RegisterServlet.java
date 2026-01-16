package com.app.controller.Auth;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.List;

import com.erp.service.UserService;
import com.erp.service.ClientService;
import com.erp.service.DelivererService;
import com.erp.model.user.User;
import com.erp.model.user.Client;
import com.erp.model.user.Deliverer;
import com.erp.model.enums.UserRole;
import com.erp.model.enums.VehicleType;
import com.app.util.EmailService;
import com.app.util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;


@WebServlet("/signup")
public class RegisterServlet extends HttpServlet {
    private UserService userService;
    private ClientService clientService;
    private DelivererService delivererService;
    
    @Override
    public void init() {
        userService = new UserService();
        clientService = new ClientService();
        delivererService = new DelivererService();
    }
    
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
        UserRole role = UserRole.valueOf(jsonObject.get("role").getAsString().toUpperCase());
        User newUser = new User(0, email , username, password, firstname, lastname, phoneNumber, false, role, new Timestamp(System.currentTimeMillis()));
        String code = JWTUtil.generateCode();
        Timestamp expiresAt = new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000);
        PrintWriter out = resp.getWriter();
        try{
            if(userService.findByEmail(email) != null){
                        out.println("{\"status\": \"fail\", \"message\": \"Email already in use\"}");
                        return;
            }
            if(userService.findByUsername(username) != null){
                       out.println("{\"status\": \"fail\", \"message\": \"Username already in use\"}");
                        return; 
            }
        }
        catch(Exception e){
            System.out.println("Error checking existing user: " + e.getMessage());
            out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
            return;
        }
        if(role == UserRole.CLIENT){
            String address = jsonObject.get("address").getAsString();
            String city = jsonObject.get("city").getAsString();
            int postalCode = jsonObject.get("postalcode").getAsInt();
            Client newClient = new Client(newUser, address, city, postalCode);
            try{
                clientService.registerClient(newClient, code, expiresAt);
                EmailService.sendVerificationEmail(newClient.getEmail() , code);
                out.println("{\"status\": \"success\", \"message\": \"Client registered successfully\"}");
            }
            catch(Exception e){
                System.out.println("Error registering client: " + e.getMessage());
                out.println("{\"status\": \"fail\"}");
            }
        }
        else if(role == UserRole.DELIVERER){
            VehicleType vehicleType = VehicleType.valueOf(jsonObject.get("vehicletype").getAsString().toUpperCase());
            String city = jsonObject.get("city").getAsString();
            double maxWeight = jsonObject.get("maxweight").getAsDouble();
            String serialNumber = jsonObject.get("serialnumber").getAsString();
            
            try {
                // Check if serial number exists
                if(delivererService.isSerialNumberExists(serialNumber)) {
                    out.println("{\"status\": \"fail\", \"message\": \"Serial number already in use\"}");
                    return;
                }
            } catch (Exception e) {
                System.out.println("Error checking serial number: " + e.getMessage());
                out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
                return;
            }
            
            Deliverer newDeliverer = new Deliverer(newUser, vehicleType, (float)maxWeight, 0.0f, serialNumber, city, true, false);
            
            try{
                delivererService.registerDeliverer(newDeliverer, code, expiresAt);
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
