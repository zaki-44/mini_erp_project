package com.app.controller;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.app.dao.Implementation.UserDAO;
import com.app.model.User;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * Servlet pour gérer le login
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private UserDAO userDAO;

    // Clé JWT fixe pour ne pas la recréer à chaque login
    private static final SecretKey JWT_KEY = 
        Keys.hmacShaKeyFor("12345678901234567890123456789012".getBytes());

    @Override
    public void init() {
        userDAO = new UserDAO();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");
        String password = request.getParameter("password");

        try {
            User user = userDAO.getUserByEmail(email);
             System.out.println("Utilisateur cherché : " + email);

            if (user == null) {
                response.sendRedirect("login.jsp?error=email");
                System.out.println("Utilisateur non trouvé pour : " + email);
                return;
            }
           System.out.println("Utilisateur trouvé : " + user.getEmail());
           
            if (!BCrypt.checkpw(password, user.getPasswordHash())) {
                response.sendRedirect("login.jsp?error=password");
                return;
            }
            System.out.println("Mot de passe correct pour : " + user.getEmail());
            // ✅ Création de la session
            HttpSession session = request.getSession();
            session.setAttribute("email", user.getEmail());
            session.setAttribute("role", user.getRole());
            
            System.out.println("Session créée pour : " + user.getEmail() + " avec rôle " + user.getRole());

            // ✅ Option : générer un JWT (pour futur API)
            long expiration = 1000 * 60 * 60 * 24; // 1 jour
            String jwtToken = Jwts.builder()
                    .setSubject(user.getEmail())
                    .claim("role", user.getRole())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration))
                    .signWith(SignatureAlgorithm.HS256, JWT_KEY)
                    .compact();

            Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
            jwtCookie.setMaxAge(24 * 60 * 60);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setPath("/");
            response.addCookie(jwtCookie);
         

            // ✅ Redirection selon le rôle
            if ("admin".equals(user.getRole())) {
                response.sendRedirect("/admin/adminPage.jsp");
                System.out.println("Redirection vers le tableau de bord admin pour : " + user.getEmail());
            } else {
                response.sendRedirect("LandingPage.jsp");
                System.out.println("Redirection vers la page d'accueil pour : " + user.getEmail());
            }

        } catch (SQLException e) {
            throw new ServletException("Erreur lors de la récupération de l'utilisateur", e);
        }
    }
}
