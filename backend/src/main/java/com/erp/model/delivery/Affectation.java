package com.erp.model.delivery;

import com.erp.model.enums.AffectationStatus;
import java.sql.Timestamp;

public class Affectation {
    private int id;
    private int delivererId;
    private int packageId;
    private AffectationStatus status;
    private Timestamp assignedAt;

    // Default constructor
    public Affectation() {}

    // Constructor without ID
    public Affectation(int delivererId, int packageId, AffectationStatus status, Timestamp assignedAt) {
        this.delivererId = delivererId;
        this.packageId = packageId;
        this.status = status;
        this.assignedAt = assignedAt;
    }

    // Constructor with ID
    public Affectation(int id, int delivererId, int packageId, AffectationStatus status, Timestamp assignedAt) {
        this.id = id;
        this.delivererId = delivererId;
        this.packageId = packageId;
        this.status = status;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDelivererId() {
        return delivererId;
    }

    public void setDelivererId(int delivererId) {
        this.delivererId = delivererId;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public AffectationStatus getStatus() {
        return status;
    }

    public void setStatus(AffectationStatus status) {
        this.status = status;
    }

    public Timestamp getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Timestamp assignedAt) {
        this.assignedAt = assignedAt;
    }

    @Override
    public String toString() {
        return "Affectation{" +
                "id=" + id +
                ", delivererId=" + delivererId +
                ", packageId=" + packageId +
                ", status=" + status +
                ", assignedAt=" + assignedAt +
                '}';
    }
}
