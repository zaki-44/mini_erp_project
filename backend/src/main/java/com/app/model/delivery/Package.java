package com.app.model.delivery;

import com.app.model.enums.PackageStatus;
import com.app.model.enums.VehicleType;
import java.sql.Timestamp;

public class Package {
    private int id;
    private int clientSourceId;
    private int clientDestinationId;
    private VehicleType vehicleTypeNeeded;
    private String addressSource;
    private String addressDestination;
    private float weight;
    private float price;
    private String dimensions;
    private String description;
    private String deliveryInstructions;
    private PackageStatus status;
    private Timestamp createdAt;

    // Default constructor
    public Package() {}

    // Constructor without ID
    public Package(int clientSourceId, int clientDestinationId, VehicleType vehicleTypeNeeded,
                   String addressSource, String addressDestination, float weight, float price,
                   String dimensions, String description, String deliveryInstructions,
                   PackageStatus status, Timestamp createdAt) {
        this.clientSourceId = clientSourceId;
        this.clientDestinationId = clientDestinationId;
        this.vehicleTypeNeeded = vehicleTypeNeeded;
        this.addressSource = addressSource;
        this.addressDestination = addressDestination;
        this.weight = weight;
        this.price = price;
        this.dimensions = dimensions;
        this.description = description;
        this.deliveryInstructions = deliveryInstructions;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Constructor with ID
    public Package(int id, int clientSourceId, int clientDestinationId, VehicleType vehicleTypeNeeded,
                   String addressSource, String addressDestination, float weight, float price,
                   String dimensions, String description, String deliveryInstructions,
                   PackageStatus status, Timestamp createdAt) {
        this.id = id;
        this.clientSourceId = clientSourceId;
        this.clientDestinationId = clientDestinationId;
        this.vehicleTypeNeeded = vehicleTypeNeeded;
        this.addressSource = addressSource;
        this.addressDestination = addressDestination;
        this.weight = weight;
        this.price = price;
        this.dimensions = dimensions;
        this.description = description;
        this.deliveryInstructions = deliveryInstructions;
        this.status = status;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientSourceId() {
        return clientSourceId;
    }

    public void setClientSourceId(int clientSourceId) {
        this.clientSourceId = clientSourceId;
    }

    public int getClientDestinationId() {
        return clientDestinationId;
    }

    public void setClientDestinationId(int clientDestinationId) {
        this.clientDestinationId = clientDestinationId;
    }

    public VehicleType getVehicleTypeNeeded() {
        return vehicleTypeNeeded;
    }

    public void setVehicleTypeNeeded(VehicleType vehicleTypeNeeded) {
        this.vehicleTypeNeeded = vehicleTypeNeeded;
    }

    public String getAddressSource() {
        return addressSource;
    }

    public void setAddressSource(String addressSource) {
        this.addressSource = addressSource;
    }

    public String getAddressDestination() {
        return addressDestination;
    }

    public void setAddressDestination(String addressDestination) {
        this.addressDestination = addressDestination;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getDimensions() {
        return dimensions;
    }

    public void setDimensions(String dimensions) {
        this.dimensions = dimensions;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDeliveryInstructions() {
        return deliveryInstructions;
    }

    public void setDeliveryInstructions(String deliveryInstructions) {
        this.deliveryInstructions = deliveryInstructions;
    }

    public PackageStatus getStatus() {
        return status;
    }

    public void setStatus(PackageStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Package{" +
                "id=" + id +
                ", clientSourceId=" + clientSourceId +
                ", clientDestinationId=" + clientDestinationId +
                ", vehicleTypeNeeded=" + vehicleTypeNeeded +
                ", addressSource='" + addressSource + '\'' +
                ", addressDestination='" + addressDestination + '\'' +
                ", weight=" + weight +
                ", price=" + price +
                ", dimensions='" + dimensions + '\'' +
                ", description='" + description + '\'' +
                ", deliveryInstructions='" + deliveryInstructions + '\'' +
                ", status=" + status +
                ", createdAt=" + createdAt +
                '}';
    }
}
