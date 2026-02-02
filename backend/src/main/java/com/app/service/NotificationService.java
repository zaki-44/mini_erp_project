package com.app.service;

import com.app.dao.Implementation.NotificationDAO;
import com.app.dao.Implementation.UserDAO;
import com.app.model.Notification;
import com.app.model.Enums.NotificationType;
import com.app.util.EmailService;
import java.sql.Timestamp;
public class NotificationService {
    private NotificationDAO notificationDAO = new NotificationDAO();
    private UserDAO userDAO = new UserDAO();

    public void sendAssignmentNotification(int destinationClientId, int sourceClientId, int delivererId,
            int packageId) {
        try {
            String delivererMessage = "You have been assigned to deliver package ID: " + packageId;
            String delivererSubject = "New Delivery Assignment";
            String delivererEmail = userDAO.findById(delivererId).getEmail();

            String clientMessage = "Your package ID: " + packageId + " has been assigned to a deliverer.";
            String clientSubject = "Package Assigned to Deliverer";
            String clientEmail = userDAO.findById(destinationClientId).getEmail();

            String sourceClientEmail = userDAO.findById(sourceClientId).getEmail();
            String sourceClientSubject = "Package Assigned to Deliverer";
            String sourceClientMessage = "Your package ID: " + packageId + " has been assigned to a deliverer.";
            
            EmailService.sendEmail(sourceClientEmail, sourceClientSubject, sourceClientMessage);
            Notification sourceClientNotification = new Notification(packageId, sourceClientId, sourceClientMessage,
                    NotificationType.ASSIGNMENT, new Timestamp(System.currentTimeMillis()));
            notificationDAO.insert(sourceClientNotification);
            
            Notification clientNotification = new Notification(packageId, destinationClientId, clientMessage,
                    NotificationType.ASSIGNMENT, new Timestamp(System.currentTimeMillis()));
            notificationDAO.insert(clientNotification);
            EmailService.sendEmail(clientEmail, clientSubject, clientMessage);
            
            Notification delivererNotification = new Notification(packageId, delivererId, delivererMessage,
                    NotificationType.ASSIGNMENT, new Timestamp(System.currentTimeMillis()));
                    
            notificationDAO.insert(delivererNotification);
            EmailService.sendEmail(delivererEmail, delivererSubject, delivererMessage);
        } catch (Exception e) {
            System.out.println("Failed to send assignment notification: " + e.getMessage());
        }
    }
    public void sendCompletionNotification(int clientId, int packageId) {
        try {
            String message = "Your package ID: " + packageId + " has been delivered.";
            String subject = "Package Delivered";
            String clientEmail = userDAO.findById(clientId).getEmail();

            Notification notification = new Notification(packageId, clientId, message,
                    NotificationType.DELIVERY_CONFIRM, new Timestamp(System.currentTimeMillis()));
            notificationDAO.insert(notification);
            EmailService.sendEmail(clientEmail, subject, message);
        } catch (Exception e) {
            System.out.println("Failed to send completion notification: " + e.getMessage());
        }
    }
}
