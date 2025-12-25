package com.app.AdminCreation;

import org.springframework.security.crypto.bcrypt.BCrypt;

import com.app.dao.Implementation.UserDAO;
import com.app.model.Enums.Role;
import com.app.model.User;


public class CreateAdmin {

    public static void main(String[] args) throws Exception {

        // Informations de l'admin
        String email = "admin@example.com";
        String username = "admin";
        String password = "admin123";
        String firstName = "Admin";
        String lastName = "User";
        String phoneNumber = "0000000000"; // tu peux mettre un numéro par défaut
        Role role = Role.ADMIN; // ton enum Role

        // Hacher le mot de passe
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        // Créer l'utilisateur admin
        if (new UserDAO().userExists(email)) {
            System.out.println("Un utilisateur avec cet email existe déjà.");
            return;
        }
        User admin = new User(email, username, hashedPassword, firstName, lastName, phoneNumber, role);

        // Sauvegarder dans la DB
        UserDAO userDAO = new UserDAO();
        userDAO.insert(admin);

        System.out.println("Admin créé avec succès !");
    }
}
