package com.app.controller.Auth;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;

import com.erp.service.UserService;
import com.google.gson.*;

@WebServlet("/verifycode")
public class VerifyCodeServlet extends HttpServlet {
    private UserService userService;
    
    @Override
    public void init() {
        userService = new UserService();
    }
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String email = jsonObject.get("email").getAsString();
        String code = jsonObject.get("code").getAsString();
        try{
            boolean isVerified = userService.verifyEmail(email, code);
            
            if(isVerified){
                out.println("{\"status\": \"success\", \"message\": \"Email verified successfully\"}");
            }
            else{
                out.println("{\"status\": \"fail\", \"message\": \"Invalid or expired verification code\"}");
            }
        }
        catch(Exception e){
            System.out.println("Error verifying code: " + e.getMessage());
            out.println("{\"status\": \"fail\"}");
        }
    }
}
