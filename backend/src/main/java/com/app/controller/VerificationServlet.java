package com.app.controller;

import java.io.IOException;

import com.app.dao.Implementation.UserDAO;
import com.app.model.User;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
/**
 * Servlet pour gérer la vérification des utilisateurs
 */
@WebServlet("/verify")
public class VerificationServlet extends HttpServlet {

    private UserDAO userDAO;

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String inputCode = request.getParameter("verificationCode");
        HttpSession session = request.getSession();
        User tempUser = (User) session.getAttribute("vrificationCode"); // utilisateur temporaire depuis signup
        System.out.println("Code entré : " + inputCode);
        if (tempUser == null) {
            response.sendRedirect("../signup.jsp");
            return;
        }
        System.out.println("Code attendu : " + tempUser.getVerificationCode());
        // Vérifier le code
        if (tempUser.getVerificationCode().equals(inputCode)) {
            tempUser.setVerified(true);

            try {
                // Sauvegarder l'utilisateur vérifié dans la DB
                userDAO.insert(tempUser);

                // Créer session définitive pour l'utilisateur
                session.setAttribute("email", tempUser.getEmail());
                session.setAttribute("role", tempUser.getRole());

                // Supprimer l'utilisateur temporaire de la session
                session.removeAttribute("vrificationCode");

                response.sendRedirect("LandingPage.jsp");
            } catch (Exception e) {
                throw new ServletException("Erreur lors de la sauvegarde de l'utilisateur", e);
            }

        } else {
            response.getWriter().println("Code incorrect !");
        }
    }
}
