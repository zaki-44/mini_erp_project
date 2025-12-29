package com.app.controller;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;

import com.app.dao.Implementation.*;
import com.google.gson.*;

import com.app.model.*;
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
            int id = userDAO.getUserIdByEmail(email);
            EmailVerificationDAO verificationDAO = new EmailVerificationDAO();
            boolean isValid = verificationDAO.verifyCode(id, code);
            if(isValid){
                User user = userDAO.findById(id);
                user.setEmailVerified(true);
                userDAO.update(user);
                out.println("{\"status\": \"success\", \"message\": \"Email verified successfully\"}");
            }
            else{
                out.println("{\"status\": \"fail\", \"message\": \"Invalid verification code\"}");
            }
        }
        catch(Exception e){
            System.out.println("Error verifying code: " + e.getMessage());
            out.println("{\"status\": \"fail\"}");
        }
    }
}
