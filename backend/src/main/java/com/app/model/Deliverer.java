package com.app.model;
import com.app.model.Enums.Role;
import com.app.model.Enums.VehicleType;


public class Deliverer extends User {
    private VehicleType vehicleType;   // e.g., bike, car, truck
    private boolean available;    // if the deliverer is available for orders
    private double maxWeight; // maximum weight the deliverer can carry
    private String city;
    private double currentLoad;
    private String serialNumber;
    private double rate;
    public Deliverer() {}

    public Deliverer(int id, String email, String username, String passwordHash,
                     String firstName, String lastName, String phoneNumber,boolean emailVerified,String city, Role role,
                     VehicleType vehicleType, boolean available, double maxWeight, double currentLoad, String serialNumber, double rate) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber, role , emailVerified);
        this.vehicleType = vehicleType;
        this.city = city;
        this.available = available;
        this.maxWeight = maxWeight;
        this.currentLoad = currentLoad;
        this.serialNumber = serialNumber;
        this.rate = rate;
    }
    public Deliverer(User user, String city, Role role,
                     VehicleType vehicleType, boolean available, double maxWeight, double currentLoad, String serialNumber, double rate) {
        super(user.getId(), user.getEmail(), user.getUsername(), user.getPasswordHash(),
              user.getFirstName(), user.getLastName(), user.getPhoneNumber(), Role.valueOf(user.getRole()) , user.isEmailVerified());
        this.vehicleType = vehicleType;
        this.city = city;
        this.available = available;
        this.maxWeight = maxWeight;
        this.currentLoad = currentLoad;
        this.serialNumber = serialNumber;
        this.rate = rate;
    }

    // Getters and Setters
    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public double getCurrentLoad() { return currentLoad; }
    public void setCurrentLoad(double currentLoad) { this.currentLoad = currentLoad; }
    public String getSerialNumber() { return serialNumber; }
    public void setSerialNumber(String serialNumber) { this.serialNumber = serialNumber; }
    public double getRate() { return rate; }
    public void setRate(double rate) { this.rate = rate; }
    @Override
    public void print(){
        super.print();
        System.out.println("Deliverer [vehicleType=" + vehicleType + ", available=" + available + ", rate=" + rate + ", maxWeight=" + maxWeight + ", currentLoad=" + currentLoad + ", city=" + city + ", serialNumber=" + serialNumber + "]");
    }
    public String toString() {
        return "Deliverer{" +
                "  id=" + getId() +
                ", email='" + getEmail() + '\'' +
                ", username='" + getUsername() + '\'' +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", phoneNumber='" + getPhoneNumber() + '\'' +
                ", vehicleType=" + vehicleType +
                ", available=" + available +
                ", maxWeight=" + maxWeight +
                ", currentLoad=" + currentLoad +
                ", city='" + city + '\'' +
                ", serialNumber='" + serialNumber + '\'' +
                ", rate=" + rate +
                '}';
    }
}
