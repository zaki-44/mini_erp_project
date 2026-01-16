package com.app.model.delivery;

import com.app.model.enums.NotificationType;
import java.sql.Timestamp;

public class Notification {
    private int id;
    private int packageId;
    private int userTargetId;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private Timestamp dateNotif;

    // Default constructor
    public Notification() {}

    // Constructor without ID
    public Notification(int packageId, int userTargetId, String message,
                        NotificationType type, boolean isRead, Timestamp dateNotif) {
        this.packageId = packageId;
        this.userTargetId = userTargetId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.dateNotif = dateNotif;
    }

    // Constructor with ID
    public Notification(int id, int packageId, int userTargetId, String message,
                        NotificationType type, boolean isRead, Timestamp dateNotif) {
        this.id = id;
        this.packageId = packageId;
        this.userTargetId = userTargetId;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.dateNotif = dateNotif;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPackageId() {
        return packageId;
    }

    public void setPackageId(int packageId) {
        this.packageId = packageId;
    }

    public int getUserTargetId() {
        return userTargetId;
    }

    public void setUserTargetId(int userTargetId) {
        this.userTargetId = userTargetId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public NotificationType getType() {
        return type;
    }

    public void setType(NotificationType type) {
        this.type = type;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public Timestamp getDateNotif() {
        return dateNotif;
    }

    public void setDateNotif(Timestamp dateNotif) {
        this.dateNotif = dateNotif;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", packageId=" + packageId +
                ", userTargetId=" + userTargetId +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", isRead=" + isRead +
                ", dateNotif=" + dateNotif +
                '}';
    }
}
