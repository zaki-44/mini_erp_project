package com.erp.model.rating;

import java.sql.Timestamp;

public class Rate {
    private int id;
    private int clientId;
    private int delivererId;
    private float score;
    private String comment;
    private Timestamp createdAt;

    // Default constructor
    public Rate() {}

    // Constructor without ID
    public Rate(int clientId, int delivererId, float score, String comment, Timestamp createdAt) {
        this.clientId = clientId;
        this.delivererId = delivererId;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Constructor with ID
    public Rate(int id, int clientId, int delivererId, float score, String comment, Timestamp createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.delivererId = delivererId;
        this.score = score;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getClientId() {
        return clientId;
    }

    public void setClientId(int clientId) {
        this.clientId = clientId;
    }

    public int getDelivererId() {
        return delivererId;
    }

    public void setDelivererId(int delivererId) {
        this.delivererId = delivererId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Rate{" +
                "id=" + id +
                ", clientId=" + clientId +
                ", delivererId=" + delivererId +
                ", score=" + score +
                ", comment='" + comment + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
