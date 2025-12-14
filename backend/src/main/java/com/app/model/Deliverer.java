package com.app.model;
import com.app.model.Enums.Role;
import com.app.model.Enums.VehicleType;


public class Deliverer extends User {
    private VehicleType vehicleType;   // e.g., bike, car, truck
    private boolean available;    // if the deliverer is available for orders
    private double maxWeight; // maximum weight the deliverer can carry
    public Deliverer() {}

    public Deliverer(int id, String email, String username, String passwordHash,
                     String firstName, String lastName, String phoneNumber, Role role,
                     VehicleType vehicleType, boolean available, double maxWeight) {
        super(id, email, username, passwordHash, firstName, lastName, phoneNumber, role);
        this.vehicleType = vehicleType;
        this.available = available;
        this.maxWeight = maxWeight;
    }

    // Getters and Setters
    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }
    
    public VehicleType getVehicleType() { return vehicleType; }
    public void setVehicleType(VehicleType vehicleType) { this.vehicleType = vehicleType; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    @Override
    public void print(){
        super.print();
        System.out.println("Deliverer [vehicleType=" + vehicleType + ", available=" + available + "]");
    }
}
