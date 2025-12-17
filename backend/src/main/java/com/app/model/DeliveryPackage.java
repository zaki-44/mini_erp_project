package com.app.model;
import java.sql.Timestamp;
import com.app.model.Enums.*;


public class DeliveryPackage {
    private int idPackage;
    private int idClientSource;
    private int idClientDestination; // nullable if not assigned yet
    private VehicleType vehicleTypeNeeded;
    private String addressSource;
    private String addressDestination;
    private double weight;
    private double price;
    private String dimensions;
    private String description;
    private PackageStatus status;
    private Timestamp createdAt;
    private String deliveryInstructions;
    public DeliveryPackage() {
        this.status = PackageStatus.CREATED;
    }

    public DeliveryPackage(int idPackage, int idClientSource, int idClientDestination,
                           VehicleType vehicleTypeNeeded, String addressSource, String addressDestination,
                           double weight, double price, String dimensions, String description,String deliveryInstructions,
                           PackageStatus status, Timestamp createdAt) {
        this.idPackage = idPackage;
        this.idClientSource = idClientSource;
        this.idClientDestination = idClientDestination;
        this.vehicleTypeNeeded = vehicleTypeNeeded;
        this.addressSource = addressSource;
        this.addressDestination = addressDestination;
        this.weight = weight;
        this.price = price;
        this.dimensions = dimensions;
        this.description = description;
        this.status = status != null ? status : PackageStatus.CREATED;
        this.createdAt = createdAt;
        this.deliveryInstructions = deliveryInstructions;
    }

    // Getters and Setters
    public int getIdPackage() { return idPackage; }
    public void setIdPackage(int idPackage) { this.idPackage = idPackage; }

    public int getIdClientSource() { return idClientSource; }
    public void setIdClientSource(int idClientSource) { this.idClientSource = idClientSource; }

    public int getIdClientDestination() { return idClientDestination; }
    public void setIdClientDestination(int idClientDestination) { this.idClientDestination = idClientDestination; }

    public VehicleType getVehicleTypeNeeded() { return vehicleTypeNeeded; }
    public void setVehicleTypeNeeded(VehicleType vehicleTypeNeeded) { this.vehicleTypeNeeded = vehicleTypeNeeded; }

    public String getAddressSource() { return addressSource; }
    public void setAddressSource(String addressSource) { this.addressSource = addressSource; }

    public String getAddressDestination() { return addressDestination; }
    public void setAddressDestination(String addressDestination) { this.addressDestination = addressDestination; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getDimensions() { return dimensions; }
    public void setDimensions(String dimensions) { this.dimensions = dimensions; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public PackageStatus getStatus() { return status; }
    public void setStatus(PackageStatus status) { this.status = status; }

    public String getDeliveryInstructions() { return deliveryInstructions; }
    public void setDeliveryInstructions(String deliveryInstructions) { this.deliveryInstructions = deliveryInstructions; }


    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void print(){
        System.out.println("DeliveryPackage [idPackage=" + idPackage + ", idClientSource=" + idClientSource +
        ", idClientDestination=" + idClientDestination + ", vehicleTypeNeeded=" + vehicleTypeNeeded +
        ", addressSource=" + addressSource + ", addressDestination=" + addressDestination +
        ", weight=" + weight + ", price=" + price + ", dimensions=" + dimensions +
        ", description=" + description + ", status=" + status + ", createdAt=" + createdAt + "]");
    }
}