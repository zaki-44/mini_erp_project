package com.app.model;

import java.sql.Date;

public class Rate {
    private int id;
    private int idDeliverer;
    private int idClient;
    private double rating;
    private String comment;
    private Date createdAt;
    public Rate(int id, int idDeliverer, int idClient, double rating, String comment, Date createdAt) {
        this.id = id;
        this.idDeliverer = idDeliverer;
        this.idClient = idClient;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }
    public Rate(int idDeliverer, int idClient, double rating, String comment, Date createdAt) {
        this.idDeliverer = idDeliverer;
        this.idClient = idClient;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }
    public int getId() {
        return id;
    }
    public int getIdDeliverer() {
        return idDeliverer;
    }
    public int getIdClient() {
        return idClient;
    }
    public double getRating() {
        return rating;
    }
    public String getComment() {
        return comment;
    }
    public Date getCreatedAt() {
        return createdAt;
    }
    public void setRating(double rating) {
        this.rating = rating;
    }
    public void setComment(String comment) {
        this.comment = comment;
    }
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    public void setId(int id) {
        this.id = id;
    }
    public void setIdDeliverer(int idDeliverer) {
        this.idDeliverer = idDeliverer;
    }
    public void setIdClient(int idClient) {
        this.idClient = idClient;
    }
}
