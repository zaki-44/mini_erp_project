package com.app.model;
import com.app.model.Enums.AffectationStatus;
import java.sql.Timestamp;

public class Affectation {
    private int idAffectation;
    private int idDeliverer;
    private int idPackage;
    private AffectationStatus status;
    private Timestamp assignedAt;

    public Affectation() {
        this.status = AffectationStatus.PENDING;
    }

    public Affectation(int idAffectation, int idDeliverer, int idPackage,
                       AffectationStatus status, Timestamp assignedAt) {
        this.idAffectation = idAffectation;
        this.idDeliverer = idDeliverer;
        this.idPackage = idPackage;
        this.status = status != null ? status : AffectationStatus.PENDING;
        this.assignedAt = assignedAt;
    }

    // Getters and Setters
    public int getIdAffectation() { return idAffectation; }
    public void setIdAffectation(int idAffectation) { this.idAffectation = idAffectation; }

    public int getIdDeliverer() { return idDeliverer; }
    public void setIdDeliverer(int idDeliverer) { this.idDeliverer = idDeliverer; }

    public int getIdPackage() { return idPackage; }
    public void setIdPackage(int idPackage) { this.idPackage = idPackage; }

    public AffectationStatus getStatus() { return status; }
    public void setStatus(AffectationStatus status) { this.status = status; }

    public Timestamp getAssignedAt() { return assignedAt; }
    public void setAssignedAt(Timestamp assignedAt) { this.assignedAt = assignedAt; }
}
