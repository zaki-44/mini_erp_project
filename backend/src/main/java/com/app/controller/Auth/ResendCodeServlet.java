package com.app.controller.Auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import com.erp.service.UserService;
import com.erp.model.user.User;
import com.app.util.EmailService;
import com.app.util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
@WebServlet("/resend-code")
public class ResendCodeServlet extends HttpServlet {
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
        String email = jsonObject.get("email").getAsString();
        PrintWriter out = resp.getWriter();
        try{
            User user = userService.findByEmail(email);
            if(user == null){
                out.println("{\"status\": \"fail\", \"message\": \"Email not found\"}");
                return;
            }
            String code = JWTUtil.generateCode();
            java.sql.Timestamp expiresAt = new java.sql.Timestamp(System.currentTimeMillis() + 15 * 60 * 1000);
            userService.resendVerificationCode(email, code, expiresAt);
            
            EmailService.sendVerificationEmail(email, code);
            out.println("{\"status\": \"success\", \"message\": \"Verification code resent\"}");
        }
        catch(Exception e){
            System.out.println("Error resending verification code: " + e.getMessage());
            out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
        }
    }    
}
