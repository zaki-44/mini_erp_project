package com.app.model;

import com.app.model.Enums.Role;
import java.util.List;

public class Client extends User {

    // Basic address info
    private String address;
    private String city;
    private int postalCode;

    // Verification status
    private boolean emailVerified;

    // Relationships (packages)
    private List<Package> sentPackages;
    private List<Package> receivedPackages;

    // Default constructor (important for frameworks)
    public Client() {}

    // Full constructor
    public Client(
            int id,
            String email,
            String username,
            String passwordHash,
            String firstName,
            String lastName,
            String phoneNumber,
            Role role,
            String address,
            String city,
            int postalCode,
            boolean phoneVerified,
            boolean emailVerified) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber, role);
        this.address = address;
        this.city = city;
        this.postalCode = postalCode;
        this.emailVerified = emailVerified;
      
        
    }

    // Getters & Setters

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

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }


    
 
    public List<Package> getSentPackages() {
        return sentPackages;
    }

    public void setSentPackages(List<Package> sentPackages) {
        this.sentPackages = sentPackages;
    }

    public List<Package> getReceivedPackages() {
        return receivedPackages;
    }

    public void setReceivedPackages(List<Package> receivedPackages) {
        this.receivedPackages = receivedPackages;
    }

    // Override print method
    @Override
    public void print() {
        super.print();
        System.out.println(
            "Client [address=" + address +
            ", city=" + city +
            ", postalCode=" + postalCode +
            
            "]"
        );
    }
}