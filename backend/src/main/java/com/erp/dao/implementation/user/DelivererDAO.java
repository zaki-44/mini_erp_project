package com.erp.dao.implementation.user;

import com.erp.dao.interfaces.GenericDAO;
import com.erp.model.user.Deliverer;
import com.erp.model.enums.UserRole;
import com.erp.model.enums.VehicleType;
import com.erp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DelivererDAO implements GenericDAO<Deliverer> {

    private Deliverer mapResultSetToEntity(ResultSet rs) throws SQLException {
        Deliverer deliverer = new Deliverer();
        deliverer.setId(rs.getInt("id"));
        deliverer.setEmail(rs.getString("email"));
        deliverer.setUsername(rs.getString("username"));
        deliverer.setPasswordHash(rs.getString("password_hash"));
        deliverer.setFirstName(rs.getString("first_name"));
        deliverer.setLastName(rs.getString("last_name"));
        deliverer.setPhoneNumber(rs.getString("phone_number"));
        deliverer.setEmailVerified(rs.getBoolean("email_verified"));
        deliverer.setCreatedAt(rs.getTimestamp("created_at"));
        deliverer.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
        deliverer.setMaxWeight(rs.getFloat("max_weight"));
        deliverer.setCurrentLoad(rs.getFloat("current_load"));
        deliverer.setSerialNumber(rs.getString("serial_number"));
        deliverer.setCity(rs.getString("city"));
        deliverer.setAvailable(rs.getBoolean("is_available"));
        deliverer.setApproved(rs.getBoolean("is_approved"));
        return deliverer;
    }

    @Override
    public void insert(Deliverer deliverer) throws SQLException {
        String insertUser = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, email_verified, role) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertDeliverer = "INSERT INTO deliverer (id, vehicle_type, max_weight, current_load, serial_number, city, is_available, is_approved) " +
                                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Insert user
            PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, deliverer.getEmail());
            userStmt.setString(2, deliverer.getUsername());
            userStmt.setString(3, deliverer.getPasswordHash());
            userStmt.setString(4, deliverer.getFirstName());
            userStmt.setString(5, deliverer.getLastName());
            userStmt.setString(6, deliverer.getPhoneNumber());
            userStmt.setBoolean(7, deliverer.isEmailVerified());
            userStmt.setString(8, "DELIVERER");
            userStmt.executeUpdate();
            
            // Get generated ID
            ResultSet rs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }
            rs.close();
            userStmt.close();
            
            deliverer.setId(userId);
            
            // Insert deliverer
            PreparedStatement delivererStmt = conn.prepareStatement(insertDeliverer);
            delivererStmt.setInt(1, userId);
            delivererStmt.setString(2, deliverer.getVehicleType().name());
            delivererStmt.setFloat(3, deliverer.getMaxWeight());
            delivererStmt.setFloat(4, deliverer.getCurrentLoad());
            delivererStmt.setString(5, deliverer.getSerialNumber());
            delivererStmt.setString(6, deliverer.getCity());
            delivererStmt.setBoolean(7, deliverer.isAvailable());
            delivererStmt.setBoolean(8, deliverer.isApproved());
            delivererStmt.executeUpdate();
            delivererStmt.close();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error inserting deliverer: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public void update(Deliverer deliverer) throws SQLException {
        String updateUser = "UPDATE users SET email=?, username=?, password_hash=?, first_name=?, last_name=?, " +
                           "phone_number=?, email_verified=?, role=? WHERE id=?";
        String updateDeliverer = "UPDATE deliverer SET vehicle_type=?, max_weight=?, current_load=?, serial_number=?, " +
                                "city=?, is_available=?, is_approved=? WHERE id=?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Update user
            PreparedStatement userStmt = conn.prepareStatement(updateUser);
            userStmt.setString(1, deliverer.getEmail());
            userStmt.setString(2, deliverer.getUsername());
            userStmt.setString(3, deliverer.getPasswordHash());
            userStmt.setString(4, deliverer.getFirstName());
            userStmt.setString(5, deliverer.getLastName());
            userStmt.setString(6, deliverer.getPhoneNumber());
            userStmt.setBoolean(7, deliverer.isEmailVerified());
            userStmt.setString(8, "DELIVERER");
            userStmt.setInt(9, deliverer.getId());
            userStmt.executeUpdate();
            userStmt.close();
            
            // Update deliverer
            PreparedStatement delivererStmt = conn.prepareStatement(updateDeliverer);
            delivererStmt.setString(1, deliverer.getVehicleType().name());
            delivererStmt.setFloat(2, deliverer.getMaxWeight());
            delivererStmt.setFloat(3, deliverer.getCurrentLoad());
            delivererStmt.setString(4, deliverer.getSerialNumber());
            delivererStmt.setString(5, deliverer.getCity());
            delivererStmt.setBoolean(6, deliverer.isAvailable());
            delivererStmt.setBoolean(7, deliverer.isApproved());
            delivererStmt.setInt(8, deliverer.getId());
            delivererStmt.executeUpdate();
            delivererStmt.close();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error updating deliverer: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String deleteDeliverer = "DELETE FROM deliverer WHERE id=?";
        String deleteUser = "DELETE FROM users WHERE id=?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement delivererStmt = conn.prepareStatement(deleteDeliverer);
            delivererStmt.setInt(1, id);
            delivererStmt.executeUpdate();
            delivererStmt.close();
            
            PreparedStatement userStmt = conn.prepareStatement(deleteUser);
            userStmt.setInt(1, id);
            userStmt.executeUpdate();
            userStmt.close();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error deleting deliverer: " + e.getMessage());
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    System.err.println("Failed to close connection: " + closeEx.getMessage());
                }
            }
        }
    }

    @Override
    public Deliverer findById(int id) throws SQLException {
        String sql = "SELECT u.*, d.* FROM users u JOIN deliverer d ON u.id = d.id WHERE u.id=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding deliverer by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Deliverer> findAll() throws SQLException {
        List<Deliverer> deliverers = new ArrayList<>();
        String sql = "SELECT u.*, d.* FROM users u JOIN deliverer d ON u.id = d.id";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                deliverers.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all deliverers: " + e.getMessage());
            throw e;
        }
        
        return deliverers;
    }

    // Additional methods
    public Deliverer findByUserId(int userId) throws SQLException {
        return findById(userId);
    }

    public List<Deliverer> findAvailableDeliverers() throws SQLException {
        List<Deliverer> deliverers = new ArrayList<>();
        String sql = "SELECT u.*, d.* FROM users u JOIN deliverer d ON u.id = d.id WHERE d.is_available = TRUE AND d.is_approved = TRUE";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                deliverers.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding available deliverers: " + e.getMessage());
            throw e;
        }
        
        return deliverers;
    }

    public List<Deliverer> findByCity(String city) throws SQLException {
        List<Deliverer> deliverers = new ArrayList<>();
        String sql = "SELECT u.*, d.* FROM users u JOIN deliverer d ON u.id = d.id WHERE d.city=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, city);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    deliverers.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding deliverers by city: " + e.getMessage());
            throw e;
        }
        
        return deliverers;
    }
}
