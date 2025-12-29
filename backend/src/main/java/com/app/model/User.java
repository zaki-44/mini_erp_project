package com.app.model;
import com.app.model.Enums.Role;


public class User {
    private int id;
    private String email;
    private String username;
    private String passwordHash;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
    private boolean emailVerified;

    // Constructors
    public User() {}

    public User(int id, String email, String username, String passwordHash,
                String firstName, String lastName, String phoneNumber, Role role , boolean emailVerified) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.emailVerified = emailVerified;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getRole() { return role.name(); }
    public void setRole(Role role) { this.role = role; }

    public boolean isEmailVerified() {
        return emailVerified;
    }
    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }
    
    public void print(){
        System.out.println("User [id=" + id + ", email=" + email + ", username=" + username + ", passwordHash=" + passwordHash
        + ", firstName=" + firstName + ", lastName=" + lastName + ", phoneNumber=" + phoneNumber + ", role=" + role + "]");
    }
}
