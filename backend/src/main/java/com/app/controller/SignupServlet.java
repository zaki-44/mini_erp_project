package com.app.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.crypto.SecretKey;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.app.dao.Implementation.UserDAO;
import com.app.model.Enums.Role;
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
 * Servlet pour gérer l'inscription des utilisateurs
 */
@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private UserDAO userDAO;

    // Clé JWT fixe pour ne pas la recréer à chaque signup
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
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                String firstName = request.getParameter("firstName");
                String lastName = request.getParameter("lastName");
                String phoneNumber = request.getParameter("phoneNumber");
                Role role = request.getParameter(lastName).equals("ADMIN") ? Role.ADMIN : Role.CLIENT; //je ne sais pas quoi faire avec la declaration ici

        // Vérification si l'utilisateur existe déjà
        if (userDAO.userExists(email)) {
            response.getWriter().println("Cet email est déjà utilisé !");
            return;
        }

        // Hasher le mot de passe avec BCrypt
        
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Créer l'utilisateur avec rôle USER par défaut
        User user = new User(email, username, hashedPassword, firstName, lastName, phoneNumber, role);
         System.out.println("Utilisateur créé : " + user.getEmail());

         // Générer un code aléatoire à 6 chiffres
         String code = sendVerificationEmail.generateVerificationCode();
         user.setVerificationCode(code);
         user.setVerified(false);
         sendVerificationEmail.sendEmail(email, "Code de vérification", "Votre code est : " + code);
        
         
         HttpSession verifySession = request.getSession();
         verifySession.setAttribute("vrificationCode", user);

         response.sendRedirect("VerificationCode/VerificationPage.jsp?email=" + email);

    
        if(user.isVerified()) {
            System.out.println("Utilisateur vérifié : " + user.getEmail());
                    try {
                        userDAO.insert(user);
                    } catch (SQLException ex) {
                        System.getLogger(SignupServlet.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                    }

        // Créer la session pour l'utilisateur
        HttpSession session = request.getSession();
        session.setAttribute("email", user.getEmail());
        session.setAttribute("role", user.getRole());

        // Générer le token JWT
        long expiration = 1000 * 60 * 60 * 24; // 1 jour
        String jwtToken = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole())
                .setIssuedAt(new java.util.Date())
                .setExpiration(new java.util.Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, JWT_KEY)
                .compact();

        // Cookie pour l'email
        Cookie userCookie = new Cookie("userEmail", user.getEmail());
        userCookie.setMaxAge(24 * 60 * 60);
        userCookie.setHttpOnly(true);
        userCookie.setPath("/");
        response.addCookie(userCookie);

        // Cookie pour le token JWT
        Cookie jwtCookie = new Cookie("jwtToken", jwtToken);
        jwtCookie.setMaxAge(24 * 60 * 60);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setPath("/");
        response.addCookie(jwtCookie);

        // Redirection vers la boutique / page principale
        response.sendRedirect("LandingPage.jsp");
        } else {
            System.out.println("Utilisateur non vérifié : " + user.getEmail());
        }
        
    }
}
