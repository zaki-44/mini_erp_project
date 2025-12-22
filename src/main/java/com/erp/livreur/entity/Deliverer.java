package com.erp.livreur.entity;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "deliverer")
public class Deliverer implements Serializable {

    @Id
    private Integer id; // FK to users.id

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = false)
    private VehicleType vehicleType;

    @Column(name = "max_weight", nullable = false)
    private Float maxWeight;

    @Column(name = "wilaya")
    private String wilaya;

    @Column(name = "is_available", nullable = false)
    private boolean available;

    // Constructors
    public Deliverer() {
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public Float getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(Float maxWeight) {
        this.maxWeight = maxWeight;
    }

    public String getWilaya() {
        return wilaya;
    }

    public void setWilaya(String wilaya) {
        this.wilaya = wilaya;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public enum VehicleType {
        CAR, BIKE, TRUCK
    }
}
