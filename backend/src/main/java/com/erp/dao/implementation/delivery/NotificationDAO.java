package com.erp.dao.implementation.delivery;

import com.erp.dao.interfaces.GenericDAO;
import com.erp.model.delivery.Notification;
import com.erp.model.enums.NotificationType;
import com.erp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO implements GenericDAO<Notification> {

    private Notification mapResultSetToEntity(ResultSet rs) throws SQLException {
        Notification notification = new Notification();
        notification.setId(rs.getInt("id_notification"));
        notification.setPackageId(rs.getInt("id_package"));
        notification.setUserTargetId(rs.getInt("id_user_target"));
        notification.setMessage(rs.getString("message"));
        notification.setType(NotificationType.valueOf(rs.getString("type")));
        notification.setRead(rs.getBoolean("is_read"));
        notification.setDateNotif(rs.getTimestamp("date_notif"));
        return notification;
    }

    // Atomic operation with provided connection
    public void insert(Connection conn, Notification notification) throws SQLException {
        String sql = "INSERT INTO notification (id_package, id_user_target, message, type, is_read) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (notification.getPackageId() == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, notification.getPackageId());
            }
            stmt.setInt(2, notification.getUserTargetId());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getType().name());
            stmt.setBoolean(5, notification.isRead());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notification.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Notification notification) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            insert(conn, notification);
        }
    }

    // Atomic operation with provided connection
    public void update(Connection conn, Notification notification) throws SQLException {
        String sql = "UPDATE notification SET id_package=?, id_user_target=?, message=?, type=?, is_read=? WHERE id_notification=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (notification.getPackageId() == 0) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, notification.getPackageId());
            }
            stmt.setInt(2, notification.getUserTargetId());
            stmt.setString(3, notification.getMessage());
            stmt.setString(4, notification.getType().name());
            stmt.setBoolean(5, notification.isRead());
            stmt.setInt(6, notification.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Notification notification) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            update(conn, notification);
        }
    }

    // Atomic operation with provided connection
    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM notification WHERE id_notification=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            delete(conn, id);
        }
    }

    @Override
    public Notification findById(int id) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id_notification=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding notification by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                notifications.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all notifications: " + e.getMessage());
            throw e;
        }
        
        return notifications;
    }

    // Additional methods
    public List<Notification> findByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE id_user_target=? ORDER BY date_notif DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding notifications by user: " + e.getMessage());
            throw e;
        }
        
        return notifications;
    }

    public List<Notification> findUnreadByUser(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notification WHERE id_user_target=? AND is_read=FALSE ORDER BY date_notif DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    notifications.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding unread notifications by user: " + e.getMessage());
            throw e;
        }
        
        return notifications;
    }

    public void markAsRead(int notificationId) throws SQLException {
        String sql = "UPDATE notification SET is_read=TRUE WHERE id_notification=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error marking notification as read: " + e.getMessage());
            throw e;
        }
    }
}
