package com.app.controller.Auth;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;

import com.erp.dao.implementation.verification.VerificationCodeDAO;
import com.erp.dao.implementation.user.UserDAO;
import com.google.gson.*;

import com.erp.model.user.User;
import com.erp.model.verification.VerificationCode;
@WebServlet("/verifycode")
public class VerifyCodeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        resp.setContentType("application/json");
        PrintWriter out = resp.getWriter();
        String email = jsonObject.get("email").getAsString();
        String code = jsonObject.get("code").getAsString();
        UserDAO userDAO = new UserDAO();
        try{
            User user = userDAO.findByEmail(email);
            if(user == null) {
                out.println("{\"status\": \"fail\", \"message\": \"User not found\"}");
                return;
            }
            
            VerificationCodeDAO verificationDAO = new VerificationCodeDAO();
            VerificationCode verificationCode = verificationDAO.findByEmail(email);
            
            if(verificationCode == null) {
                out.println("{\"status\": \"fail\", \"message\": \"Verification code not found\"}");
                return;
            }
            
            // Check if code matches and is not expired
            boolean isValid = verificationCode.getCode().equals(code) && 
                             verificationCode.getExpiresAt().after(new java.sql.Timestamp(System.currentTimeMillis()));
            
            if(isValid){
                user.setEmailVerified(true);
                userDAO.update(user);
                out.println("{\"status\": \"success\", \"message\": \"Email verified successfully\"}");
                verificationDAO.delete(email);
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
