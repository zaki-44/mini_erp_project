package com.erp.service;

import com.erp.dao.implementation.delivery.PackageDAO;
import com.erp.dao.implementation.delivery.NotificationDAO;
import com.erp.model.delivery.Package;
import com.erp.model.delivery.Notification;
import com.erp.model.enums.NotificationType;
import com.erp.model.enums.PackageStatus;
import com.erp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class PackageService {
    private PackageDAO packageDAO;
    private NotificationDAO notificationDAO;
    
    public PackageService() {
        this.packageDAO = new PackageDAO();
        this.notificationDAO = new NotificationDAO();
    }
    
    public Package createPackage(Package pkg) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            packageDAO.insert(conn, pkg);
            
            Notification notification = new Notification();
            notification.setPackageId(pkg.getId());
            notification.setUserTargetId(pkg.getClientSourceId());
            notification.setMessage("Package created successfully");
            notification.setType(NotificationType.STATUS_UPDATE);
            notification.setRead(false);
            notificationDAO.insert(conn, notification);
            
            conn.commit();
            return pkg;
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
    
    public Package findById(int id) throws SQLException {
        return packageDAO.findById(id);
    }
    
    public List<Package> getAllPackages() throws SQLException {
        return packageDAO.findAll();
    }
    
    public List<Package> getPackagesByClientSource(int clientId) throws SQLException {
        return packageDAO.findByClientSource(clientId);
    }
    
    public List<Package> getPackagesByClientDestination(int clientId) throws SQLException {
        return packageDAO.findByClientDestination(clientId);
    }
    
    public List<Package> getPackagesByStatus(String status) throws SQLException {
        return packageDAO.findByStatus(status);
    }
    
    public void updatePackage(Package pkg) throws SQLException {
        packageDAO.update(pkg);
    }
    
    public void updatePackageStatus(int packageId, PackageStatus newStatus, int userTargetId) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            Package pkg = packageDAO.findById(packageId);
            if (pkg == null) {
                throw new SQLException("Package not found with id: " + packageId);
            }
            
            pkg.setStatus(newStatus);
            packageDAO.update(conn, pkg);
            
            Notification notification = new Notification();
            notification.setPackageId(packageId);
            notification.setUserTargetId(userTargetId);
            notification.setMessage("Package status updated to: " + newStatus.name());
            notification.setType(NotificationType.STATUS_UPDATE);
            notification.setRead(false);
            notificationDAO.insert(conn, notification);
            
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
    
    public void deletePackage(int id) throws SQLException {
        packageDAO.delete(id);
    }
}
