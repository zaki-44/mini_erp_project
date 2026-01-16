package com.erp.service;

import com.erp.dao.implementation.user.DelivererDAO;
import com.erp.dao.implementation.verification.VerificationCodeDAO;
import com.erp.model.user.Deliverer;
import com.erp.model.verification.VerificationCode;
import com.erp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class DelivererService {
    private DelivererDAO delivererDAO;
    private VerificationCodeDAO verificationCodeDAO;
    
    public DelivererService() {
        this.delivererDAO = new DelivererDAO();
        this.verificationCodeDAO = new VerificationCodeDAO();
    }
    
    public Deliverer registerDeliverer(Deliverer deliverer, String verificationCode, Timestamp expiresAt) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            delivererDAO.insert(conn, deliverer);
            
            VerificationCode vc = new VerificationCode();
            vc.setEmail(deliverer.getEmail());
            vc.setCode(verificationCode);
            vc.setExpiresAt(expiresAt);
            verificationCodeDAO.insert(conn, vc);
            
            conn.commit();
            return deliverer;
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
    
    public Deliverer findById(int id) throws SQLException {
        return delivererDAO.findById(id);
    }
    
    public Deliverer findByUserId(int userId) throws SQLException {
        return delivererDAO.findByUserId(userId);
    }
    
    public List<Deliverer> getAllDeliverers() throws SQLException {
        return delivererDAO.findAll();
    }
    
    public List<Deliverer> getAvailableDeliverers() throws SQLException {
        return delivererDAO.findAvailableDeliverers();
    }
    
    public List<Deliverer> getDeliverersByCity(String city) throws SQLException {
        return delivererDAO.findByCity(city);
    }
    
    public void updateDeliverer(Deliverer deliverer) throws SQLException {
        delivererDAO.update(deliverer);
    }
    
    public void deleteDeliverer(int id) throws SQLException {
        delivererDAO.delete(id);
    }
    
    public void approveDeliverer(int delivererId) throws SQLException {
        Deliverer deliverer = delivererDAO.findById(delivererId);
        if (deliverer != null) {
            deliverer.setApproved(true);
            delivererDAO.update(deliverer);
        }
    }
    
    public void setDelivererAvailability(int delivererId, boolean available) throws SQLException {
        Deliverer deliverer = delivererDAO.findById(delivererId);
        if (deliverer != null) {
            deliverer.setAvailable(available);
            delivererDAO.update(deliverer);
        }
    }
}
