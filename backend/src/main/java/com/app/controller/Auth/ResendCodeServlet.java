package com.app.controller.Auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import com.erp.dao.implementation.user.UserDAO;
import com.erp.dao.implementation.verification.VerificationCodeDAO;
import com.erp.model.user.User;
import com.erp.model.verification.VerificationCode;
import com.app.util.EmailService;
import com.app.util.JWTUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
@WebServlet("/resend-code")
public class ResendCodeServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Gson gson = new Gson();
        BufferedReader reader = req.getReader();
        JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
        resp.setContentType("application/json");
        String email = jsonObject.get("email").getAsString();
        PrintWriter out = resp.getWriter();
        UserDAO userDAO = new UserDAO();
        try{
            User user = userDAO.findByEmail(email);
            if(user == null){
                out.println("{\"status\": \"fail\", \"message\": \"Email not found\"}");
                return;
            }
            String code = JWTUtil.generateCode();
            VerificationCodeDAO verificationDAO = new VerificationCodeDAO();
            
            // Check if verification code exists, update or insert
            VerificationCode existingCode = verificationDAO.findByEmail(email);
            if(existingCode != null) {
                existingCode.setCode(code);
                existingCode.setExpiresAt(new java.sql.Timestamp(System.currentTimeMillis() + 15 * 60 * 1000));
                verificationDAO.update(existingCode);
            } else {
                VerificationCode newCode = new VerificationCode(email, code, 
                    new java.sql.Timestamp(System.currentTimeMillis()),
                    new java.sql.Timestamp(System.currentTimeMillis() + 15 * 60 * 1000));
                verificationDAO.insert(newCode);
            }
            
            EmailService.sendVerificationEmail(email, code);
            out.println("{\"status\": \"success\", \"message\": \"Verification code resent\"}");
        }
        catch(Exception e){
            System.out.println("Error resending verification code: " + e.getMessage());
            out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
        }
    }    
}
