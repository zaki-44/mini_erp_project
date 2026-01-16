package com.app.model.users;

import com.app.model.enums.UserRole;
import com.app.model.enums.VehicleType;
import java.sql.Timestamp;

public class Deliverer extends User {
    private VehicleType vehicleType;
    private float maxWeight;
    private float currentLoad;
    private String serialNumber;
    private String city;
    private boolean isAvailable;
    private boolean isApproved;

    // Default constructor
    public Deliverer() {
        super();
    }

    // Constructor without ID
    public Deliverer(String email, String username, String passwordHash,
                     String firstName, String lastName, String phoneNumber,
                     boolean emailVerified, Timestamp createdAt,
                     VehicleType vehicleType, float maxWeight, float currentLoad,
                     String serialNumber, String city, boolean isAvailable, boolean isApproved) {
        super(email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.DELIVERER, createdAt);
        this.vehicleType = vehicleType;
        this.maxWeight = maxWeight;
        this.currentLoad = currentLoad;
        this.serialNumber = serialNumber;
        this.city = city;
        this.isAvailable = isAvailable;
        this.isApproved = isApproved;
    }

    // Constructor with ID
    public Deliverer(int id, String email, String username, String passwordHash,
                     String firstName, String lastName, String phoneNumber,
                     boolean emailVerified, Timestamp createdAt,
                     VehicleType vehicleType, float maxWeight, float currentLoad,
                     String serialNumber, String city, boolean isAvailable, boolean isApproved) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber,
              emailVerified, UserRole.DELIVERER, createdAt);
        this.vehicleType = vehicleType;
        this.maxWeight = maxWeight;
        this.currentLoad = currentLoad;
        this.serialNumber = serialNumber;
        this.city = city;
        this.isAvailable = isAvailable;
        this.isApproved = isApproved;
    }

    // Constructor from User object
    public Deliverer(User user, VehicleType vehicleType, float maxWeight, float currentLoad,
                     String serialNumber, String city, boolean isAvailable, boolean isApproved) {
        super(user.getId(), user.getEmail(), user.getUsername(), user.getPasswordHash(),
              user.getFirstName(), user.getLastName(), user.getPhoneNumber(),
              user.isEmailVerified(), UserRole.DELIVERER, user.getCreatedAt());
        this.vehicleType = vehicleType;
        this.maxWeight = maxWeight;
        this.currentLoad = currentLoad;
        this.serialNumber = serialNumber;
        this.city = city;
        this.isAvailable = isAvailable;
        this.isApproved = isApproved;
    }

    // Getters and Setters
    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public float getCurrentLoad() {
        return currentLoad;
    }

    public void setCurrentLoad(float currentLoad) {
        this.currentLoad = currentLoad;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public void setApproved(boolean approved) {
        isApproved = approved;
    }

    @Override
    public String getRole() {
        return UserRole.DELIVERER.name();
    }

    @Override
    public String toString() {
        return "Deliverer{" +
                "id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", emailVerified=" + isEmailVerified() +
                ", createdAt=" + getCreatedAt() +
                ", vehicleType=" + vehicleType +
                ", maxWeight=" + maxWeight +
                ", currentLoad=" + currentLoad +
                ", serialNumber='" + serialNumber + '\'' +
                ", city='" + city + '\'' +
                ", isAvailable=" + isAvailable +
                ", isApproved=" + isApproved +
                '}';
    }
}
