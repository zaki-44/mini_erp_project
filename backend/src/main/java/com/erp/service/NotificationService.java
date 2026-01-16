package com.erp.service;

import com.erp.dao.implementation.delivery.NotificationDAO;
import com.erp.model.delivery.Notification;

import java.sql.SQLException;
import java.util.List;

public class NotificationService {
    private NotificationDAO notificationDAO;
    
    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }
    
    public Notification createNotification(Notification notification) throws SQLException {
        notificationDAO.insert(notification);
        return notification;
    }
    
    public Notification findById(int id) throws SQLException {
        return notificationDAO.findById(id);
    }
    
    public List<Notification> getAllNotifications() throws SQLException {
        return notificationDAO.findAll();
    }
    
    public List<Notification> getNotificationsByUser(int userId) throws SQLException {
        return notificationDAO.findByUser(userId);
    }
    
    public List<Notification> getUnreadNotificationsByUser(int userId) throws SQLException {
        return notificationDAO.findUnreadByUser(userId);
    }
    
    public void markNotificationAsRead(int notificationId) throws SQLException {
        notificationDAO.markAsRead(notificationId);
    }
    
    public void markAllUserNotificationsAsRead(int userId) throws SQLException {
        List<Notification> unreadNotifications = notificationDAO.findUnreadByUser(userId);
        for (Notification notification : unreadNotifications) {
            notificationDAO.markAsRead(notification.getId());
        }
    }
    
    public void updateNotification(Notification notification) throws SQLException {
        notificationDAO.update(notification);
    }
    
    public void deleteNotification(int id) throws SQLException {
        notificationDAO.delete(id);
    }
    
    public void deleteAllUserNotifications(int userId) throws SQLException {
        List<Notification> notifications = notificationDAO.findByUser(userId);
        for (Notification notification : notifications) {
            notificationDAO.delete(notification.getId());
        }
    }
}
