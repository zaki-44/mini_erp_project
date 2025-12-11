package com.app.model;
import com.app.model.Enums.NotificationType;
import java.sql.Timestamp;

public class Notification {
    private int idNotification;
    private int idPackage;
    private int idUserTarget;
    private String message;
    private NotificationType type;
    private boolean isRead;
    private Timestamp dateNotif;

    public Notification() {
        this.isRead = false;
    }

    public Notification(int idNotification, int idPackage, int idUserTarget, String message,
                        NotificationType type, boolean isRead, Timestamp dateNotif) {
        this.idNotification = idNotification;
        this.idPackage = idPackage;
        this.idUserTarget = idUserTarget;
        this.message = message;
        this.type = type;
        this.isRead = isRead;
        this.dateNotif = dateNotif;
    }

    // Getters and Setters
    public int getIdNotification() { return idNotification; }
    public void setIdNotification(int idNotification) { this.idNotification = idNotification; }

    public int getIdPackage() { return idPackage; }
    public void setIdPackage(int idPackage) { this.idPackage = idPackage; }

    public int getIdUserTarget() { return idUserTarget; }
    public void setIdUserTarget(int idUserTarget) { this.idUserTarget = idUserTarget; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public Timestamp getDateNotif() { return dateNotif; }
    public void setDateNotif(Timestamp dateNotif) { this.dateNotif = dateNotif; }
    public void print(){
        System.out.println("Notification [idNotification=" + idNotification + ", idPackage=" + idPackage +
        ", idUserTarget=" + idUserTarget + ", message=" + message + ", type=" + type +
        ", isRead=" + isRead + ", dateNotif=" + dateNotif + "]");
    }
}
