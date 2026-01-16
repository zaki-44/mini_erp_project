package com.app.service;

import com.app.dao.implementation.delivery.AffectationDAO;
import com.app.dao.implementation.delivery.PackageDAO;
import com.app.dao.implementation.delivery.NotificationDAO;
import com.app.dao.implementation.users.DelivererDAO;
import com.app.model.delivery.Affectation;
import com.app.model.delivery.Package;
import com.app.model.delivery.Notification;
import com.app.model.users.Deliverer;
import com.app.model.enums.AffectationStatus;
import com.app.model.enums.PackageStatus;
import com.app.model.enums.NotificationType;
import com.app.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AffectationService {
    private AffectationDAO affectationDAO;
    private PackageDAO packageDAO;
    private NotificationDAO notificationDAO;
    private DelivererDAO delivererDAO;
    
    public AffectationService() {
        this.affectationDAO = new AffectationDAO();
        this.packageDAO = new PackageDAO();
        this.notificationDAO = new NotificationDAO();
        this.delivererDAO = new DelivererDAO();
    }
    
    public Affectation assignPackageToDeliverer(int packageId, int delivererId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Package pkg = packageDAO.findById(packageId);
            Deliverer deliverer = delivererDAO.findById(delivererId);
            
            if (pkg == null || deliverer == null) {
                throw new SQLException("Package or Deliverer not found");
            }
            
            Affectation affectation = new Affectation();
            affectation.setPackageId(packageId);
            affectation.setDelivererId(delivererId);
            affectation.setStatus(AffectationStatus.PENDING);
            affectationDAO.insert(conn, affectation);
            
            pkg.setStatus(PackageStatus.ASSIGNED);
            packageDAO.update(conn, pkg);
            
            Notification notifDeliverer = new Notification();
            notifDeliverer.setPackageId(packageId);
            notifDeliverer.setUserTargetId(delivererId);
            notifDeliverer.setMessage("New package assigned to you");
            notifDeliverer.setType(NotificationType.ASSIGNMENT);
            notifDeliverer.setRead(false);
            notificationDAO.insert(conn, notifDeliverer);
            
            Notification notifClient = new Notification();
            notifClient.setPackageId(packageId);
            notifClient.setUserTargetId(pkg.getClientSourceId());
            notifClient.setMessage("Your package has been assigned to a deliverer");
            notifClient.setType(NotificationType.ASSIGNMENT);
            notifClient.setRead(false);
            notificationDAO.insert(conn, notifClient);
            
            conn.commit();
            return affectation;
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
    
    public void acceptAffectation(int affectationId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Affectation affectation = affectationDAO.findById(affectationId);
            if (affectation != null) {
                affectation.setStatus(AffectationStatus.ACCEPTED);
                affectationDAO.update(conn, affectation);
                
                Package pkg = packageDAO.findById(affectation.getPackageId());
                if (pkg != null) {
                    pkg.setStatus(PackageStatus.PICKEDUP);
                    packageDAO.update(conn, pkg);
                    
                    Notification notification = new Notification();
                    notification.setPackageId(pkg.getId());
                    notification.setUserTargetId(pkg.getClientSourceId());
                    notification.setMessage("Your package has been picked up");
                    notification.setType(NotificationType.STATUS_UPDATE);
                    notification.setRead(false);
                    notificationDAO.insert(conn, notification);
                }
            }
            
            conn.commit();
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
    
    public void rejectAffectation(int affectationId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Affectation affectation = affectationDAO.findById(affectationId);
            if (affectation != null) {
                affectation.setStatus(AffectationStatus.REJECTED);
                affectationDAO.update(conn, affectation);
                
                Package pkg = packageDAO.findById(affectation.getPackageId());
                if (pkg != null) {
                    pkg.setStatus(PackageStatus.CREATED);
                    packageDAO.update(conn, pkg);
                }
            }
            
            conn.commit();
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
    
    public void completeAffectation(int affectationId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Affectation affectation = affectationDAO.findById(affectationId);
            if (affectation != null) {
                affectation.setStatus(AffectationStatus.COMPLETED);
                affectationDAO.update(conn, affectation);
                
                Package pkg = packageDAO.findById(affectation.getPackageId());
                if (pkg != null) {
                    pkg.setStatus(PackageStatus.DELIVERED);
                    packageDAO.update(conn, pkg);
                    
                    if (pkg.getClientDestinationId() != 0) {
                        Notification notification = new Notification();
                        notification.setPackageId(pkg.getId());
                        notification.setUserTargetId(pkg.getClientDestinationId());
                        notification.setMessage("Your package has been delivered");
                        notification.setType(NotificationType.DELIVERY_CONFIRM);
                        notification.setRead(false);
                        notificationDAO.insert(conn, notification);
                    }
                }
            }
            
            conn.commit();
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
    
    public Affectation findById(int id) throws SQLException {
        return affectationDAO.findById(id);
    }
    
    public List<Affectation> getAllAffectations() throws SQLException {
        return affectationDAO.findAll();
    }
    
    public List<Affectation> getAffectationsByDeliverer(int delivererId) throws SQLException {
        return affectationDAO.findByDeliverer(delivererId);
    }
    
    public List<Affectation> getAffectationsByPackage(int packageId) throws SQLException {
        return affectationDAO.findByPackage(packageId);
    }
    
    public List<Affectation> getAffectationsByStatus(String status) throws SQLException {
        return affectationDAO.findByStatus(status);
    }
    
    public void deleteAffectation(int id) throws SQLException {
        affectationDAO.delete(id);
    }
}
