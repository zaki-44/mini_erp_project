package com.app.model.users;

import com.app.model.enums.UserRole;
import java.sql.Timestamp;

public class Admin extends User {

    // Default constructor
    public Admin() {
        super();
    }

    // Constructor without ID
    public Admin(String email, String username, String passwordHash,
                 String firstName, String lastName, String phoneNumber,
                 boolean emailVerified, Timestamp createdAt) {
        super(email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.ADMIN, createdAt);
    }

    // Constructor with ID
    public Admin(int id, String email, String username, String passwordHash,
                 String firstName, String lastName, String phoneNumber,
                 boolean emailVerified, Timestamp createdAt) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.ADMIN, createdAt);
    }

    // Constructor from User object
    public Admin(User user) {
        super(user.getId(), user.getEmail(), user.getUsername(), user.getPasswordHash(),
              user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
              user.isEmailVerified(), UserRole.ADMIN, user.getCreatedAt());
    }

    @Override
    public String getRole() {
        return UserRole.ADMIN.name();
    }

    @Override
    public String toString() {
        return "Admin{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", emailVerified=" + isEmailVerified() +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}
