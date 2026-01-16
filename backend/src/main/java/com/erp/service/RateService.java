package com.erp.service;

import com.erp.dao.implementation.rating.RateDAO;
import com.erp.dao.implementation.delivery.NotificationDAO;
import com.erp.model.rating.Rate;
import com.erp.model.delivery.Notification;
import com.erp.model.enums.NotificationType;
import com.erp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RateService {
    private RateDAO rateDAO;
    private NotificationDAO notificationDAO;
    
    public RateService() {
        this.rateDAO = new RateDAO();
        this.notificationDAO = new NotificationDAO();
    }
    
    public Rate createRate(Rate rate) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            rateDAO.insert(conn, rate);
            
            Notification notification = new Notification();
            notification.setPackageId(0);
            notification.setUserTargetId(rate.getDelivererId());
            notification.setMessage("You received a new rating: " + rate.getScore() + " stars");
            notification.setType(NotificationType.RATE_RECEIVED);
            notification.setRead(false);
            notificationDAO.insert(conn, notification);
            
            conn.commit();
            return rate;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            Database.closeConnection(conn);
        }
    }
    
    public Rate findById(int id) throws SQLException {
        return rateDAO.findById(id);
    }
    
    public List<Rate> getAllRates() throws SQLException {
        return rateDAO.findAll();
    }
    
    public List<Rate> getRatesByDeliverer(int delivererId) throws SQLException {
        return rateDAO.findByDeliverer(delivererId);
    }
    
    public List<Rate> getRatesByClient(int clientId) throws SQLException {
        return rateDAO.findByClient(clientId);
    }
    
    public float getAverageRateForDeliverer(int delivererId) throws SQLException {
        return rateDAO.getAverageRateForDeliverer(delivererId);
    }
    
    public void updateRate(Rate rate) throws SQLException {
        rateDAO.update(rate);
    }
    
    public void deleteRate(int id) throws SQLException {
        rateDAO.delete(id);
    }
}
