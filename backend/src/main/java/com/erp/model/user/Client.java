package com.erp.model.user;

import com.erp.model.enums.UserRole;
import java.sql.Timestamp;

public class Client extends User {
    private String address;
    private String city;
    private int postalCode;

    // Default constructor
    public Client() {
        super();
    }

    // Constructor without ID
    public Client(String email, String username, String passwordHash,
                  String firstName, String lastName, String phoneNumber,
                  boolean emailVerified, Timestamp createdAt,
                  String address, String city, int postalCode) {
        super(email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.CLIENT, createdAt);
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    // Constructor with ID
    public Client(int id, String email, String username, String passwordHash,
                  String firstName, String lastName, String phoneNumber,
                  boolean emailVerified, Timestamp createdAt,
                  String address, String city, int postalCode) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.CLIENT, createdAt);
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    // Constructor from User object
    public Client(User user, String address, String city, int postalCode) {
        super(user.getId(), user.getEmail(), user.getUsername(), user.getPasswordHash(),
              user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
              user.isEmailVerified(), UserRole.CLIENT, user.getCreatedAt());
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
    }

    // Getters and Setters
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }

    @Override
    public String getRole() {
        return UserRole.CLIENT.name();
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", emailVerified=" + isEmailVerified() +
                ", createdAt=" + getCreatedAt() +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", postalCode=" + postalCode +
                '}';
    }
}
