package com.app.model.verification;

import java.sql.Timestamp;

public class VerificationCode {
    private String email;
    private String code;
    private Timestamp createdAt;
    private Timestamp expiresAt;

    // Default constructor
    public VerificationCode() {}

    // Constructor
    public VerificationCode(String email, String code, Timestamp createdAt, Timestamp expiresAt) {
        this.email = email;
        this.code = code;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public Timestamp getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Timestamp expiresAt) {
        this.expiresAt = expiresAt;
    }

    @Override
    public String toString() {
        return "VerificationCode{" +
                "email='" + email + '\'' +
                ", code='" + code + '\'' +
                ", createdAt=" + createdAt +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
