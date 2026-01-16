package com.app.controller.Auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import java.sql.Timestamp;
import java.util.Calendar;

import com.app.service.UserService;
import com.app.service.ClientService;
import com.app.service.DelivererService;
import com.app.model.users.User;
import com.app.model.users.Client;
import com.app.model.users.Deliverer;
import com.app.model.enums.UserRole;
import com.app.model.enums.VehicleType;
import com.app.util.JWTUtil;
import com.app.util.PasswordUtils;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

@WebServlet("/auth/*")
public class AuthServlet extends HttpServlet {
    private UserService userService;
    private ClientService clientService;
    private DelivererService delivererService;
    private Gson gson;

    @Override
    public void init() {
        userService = new UserService();
        clientService = new ClientService();
        delivererService = new DelivererService();
        gson = new Gson();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (pathInfo == null) {
            sendError(resp, "Invalid endpoint");
            return;
        }

        switch (pathInfo) {
            case "/register":
            case "/signup":
                handleRegister(req, resp);
                break;
            case "/login":
                handleLogin(req, resp);
                break;
            case "/verify":
                handleVerify(req, resp);
                break;
            case "/resend":
                handleResend(req, resp);
                break;
            case "/logout":
                handleLogout(req, resp);
                break;
            default:
                sendError(resp, "Invalid endpoint");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String pathInfo = req.getPathInfo();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if ("/logout".equals(pathInfo)) {
            handleLogout(req, resp);
        } else {
            sendError(resp, "Invalid endpoint");
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            
            String firstname = jsonObject.get("firstname").getAsString();
            String lastname = jsonObject.get("lastname").getAsString();
            String username = jsonObject.get("username").getAsString();
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            String phoneNumber = jsonObject.get("phonenumber").getAsString();
            UserRole role = UserRole.valueOf(jsonObject.get("role").getAsString().toUpperCase());
            
            String hashedPassword = PasswordUtils.hashPassword(password, email);
            String verificationCode = JWTUtil.generateCode();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 15);
            Timestamp expiresAt = new Timestamp(calendar.getTimeInMillis());
            
            // Check if user exists
            if (userService.getUserByEmail(email) != null) {
                sendError(resp, "Email already in use");
                return;
            }
            if (userService.getUserByUsername(username) != null) {
                sendError(resp, "Username already in use");
                return;
            }
            
            if (role == UserRole.CLIENT) {
                String address = jsonObject.get("address").getAsString();
                String city = jsonObject.get("city").getAsString();
                int postalCode = jsonObject.get("postalCode").getAsInt();
                
                Client client = new Client(email, username, hashedPassword, firstname, lastname,
                        phoneNumber, false, new Timestamp(System.currentTimeMillis()),
                        address, city, postalCode);
                
                clientService.registerClient(client, verificationCode, expiresAt);
                sendSuccess(resp, "Client registered successfully. Please check your email for verification code.");
                
            } else if (role == UserRole.DELIVERER) {
                String city = jsonObject.get("city").getAsString();
                VehicleType vehicleType = VehicleType.valueOf(jsonObject.get("vehicleType").getAsString().toUpperCase());
                float maxWeight = jsonObject.get("maxWeight").getAsFloat();
                String serialNumber = jsonObject.get("serialNumber").getAsString();
                
                // Check serial number
                if (delivererService.isSerialNumberExists(serialNumber)) {
                    sendError(resp, "Serial number already exists");
                    return;
                }
                
                Deliverer deliverer = new Deliverer(email, username, hashedPassword, firstname, lastname,
                        phoneNumber, false, new Timestamp(System.currentTimeMillis()),
                        vehicleType, maxWeight, 0, serialNumber, city, true, false);
                
                delivererService.registerDeliverer(deliverer, verificationCode, expiresAt);
                sendSuccess(resp, "Deliverer registered successfully. Please check your email for verification code.");
                
            } else {
                sendError(resp, "Invalid role");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Registration failed: " + e.getMessage());
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            
            String email = jsonObject.get("email").getAsString();
            String password = jsonObject.get("password").getAsString();
            
            User user = userService.login(email, password);
            
            if (user == null) {
                sendError(resp, "Invalid email or password");
                return;
            }
            
            if (!user.isEmailVerified()) {
                sendError(resp, "Email not verified. Please verify your email first.");
                return;
            }
            
            // Generate JWT token
            String token = JWTUtil.generateToken(user.getId(), user.getRole());
            
            JsonObject response = new JsonObject();
            response.addProperty("status", "success");
            response.addProperty("message", "Login successful");
            response.addProperty("token", token);
            response.addProperty("userId", user.getId());
            response.addProperty("email", user.getEmail());
            response.addProperty("role", user.getRole());
            
            resp.getWriter().println(response.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Login failed: " + e.getMessage());
        }
    }

    private void handleVerify(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            
            String email = jsonObject.get("email").getAsString();
            String code = jsonObject.get("code").getAsString();
            
            boolean verified = userService.verifyEmail(email, code);
            
            if (verified) {
                sendSuccess(resp, "Email verified successfully. You can now login.");
            } else {
                sendError(resp, "Invalid or expired verification code");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Verification failed: " + e.getMessage());
        }
    }

    private void handleResend(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            JsonObject jsonObject = gson.fromJson(req.getReader(), JsonObject.class);
            
            String email = jsonObject.get("email").getAsString();
            
            String verificationCode = JWTUtil.generateCode();
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.MINUTE, 15);
            Timestamp expiresAt = new Timestamp(calendar.getTimeInMillis());
            
            userService.resendVerificationCode(email, verificationCode, expiresAt);
            sendSuccess(resp, "Verification code resent successfully");
            
        } catch (Exception e) {
            e.printStackTrace();
            sendError(resp, "Resend failed: " + e.getMessage());
        }
    }

    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        sendSuccess(resp, "Logged out successfully");
    }

    private void sendSuccess(HttpServletResponse resp, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("status", "success");
        response.addProperty("message", message);
        resp.getWriter().println(response.toString());
    }

    private void sendError(HttpServletResponse resp, String message) throws IOException {
        JsonObject response = new JsonObject();
        response.addProperty("status", "fail");
        response.addProperty("message", message);
        resp.getWriter().println(response.toString());
    }
}
