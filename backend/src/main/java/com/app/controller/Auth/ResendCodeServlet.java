package com.app.controller.Auth;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.*;
import com.app.dao.Implementation.*;
import com.app.model.*;
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
            User user = userDAO.getByEmail(email);
            if(user == null){
                out.println("{\"status\": \"fail\", \"message\": \"Email not found\"}");
                return;
            }
            String code = JWTUtil.generateCode();
            EmailVerificationDAO verificationDAO = new EmailVerificationDAO();
            verificationDAO.updateVerificationCode(user.getId(), code);
            EmailService.sendVerificationEmail(email, code);
            out.println("{\"status\": \"success\", \"message\": \"Verification code resent\"}");
        }
        catch(Exception e){
            System.out.println("Error resending verification code: " + e.getMessage());
            out.println("{\"status\": \"fail\", \"message\": \"Server error\"}");
        }
    }    
}
