package com.erp.dao.implementation.user;

import com.erp.dao.interfaces.GenericDAO;
import com.erp.model.user.Client;
import com.erp.model.enums.UserRole;
import com.erp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClientDAO implements GenericDAO<Client> {

    private Client mapResultSetToEntity(ResultSet rs) throws SQLException {
        Client client = new Client();
        client.setId(rs.getInt("id"));
        client.setEmail(rs.getString("email"));
        client.setUsername(rs.getString("username"));
        client.setPasswordHash(rs.getString("password_hash"));
        client.setFirstName(rs.getString("first_name"));
        client.setLastName(rs.getString("last_name"));
        client.setPhoneNumber(rs.getString("phone_number"));
        client.setEmailVerified(rs.getBoolean("email_verified"));
        client.setCreatedAt(rs.getTimestamp("created_at"));
        client.setAddress(rs.getString("address"));
        client.setCity(rs.getString("city"));
        client.setPostalCode(rs.getInt("postal_code"));
        return client;
    }

    @Override
    public void insert(Client client) throws SQLException {
        String insertUser = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, email_verified, role) " +
                           "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String insertClient = "INSERT INTO client (id, address, city, postal_code) VALUES (?, ?, ?, ?)";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Insert user
            PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, client.getEmail());
            userStmt.setString(2, client.getUsername());
            userStmt.setString(3, client.getPasswordHash());
            userStmt.setString(4, client.getFirstName());
            userStmt.setString(5, client.getLastName());
            userStmt.setString(6, client.getPhoneNumber());
            userStmt.setBoolean(7, client.isEmailVerified());
            userStmt.setString(8, "CLIENT");
            userStmt.executeUpdate();
            
            // Get generated ID
            ResultSet rs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }
            rs.close();
            userStmt.close();
            
            client.setId(userId);
            
            // Insert client
            PreparedStatement clientStmt = conn.prepareStatement(insertClient);
            clientStmt.setInt(1, userId);
            clientStmt.setString(2, client.getAddress());
            clientStmt.setString(3, client.getCity());
            clientStmt.setInt(4, client.getPostalCode());
            clientStmt.executeUpdate();
            clientStmt.close();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error inserting client: " + e.getMessage());
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
    public void update(Client client) throws SQLException {
        String updateUser = "UPDATE users SET email=?, username=?, password_hash=?, first_name=?, last_name=?, " +
                           "phone_number=?, email_verified=?, role=? WHERE id=?";
        String updateClient = "UPDATE client SET address=?, city=?, postal_code=? WHERE id=?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Update user
            PreparedStatement userStmt = conn.prepareStatement(updateUser);
            userStmt.setString(1, client.getEmail());
            userStmt.setString(2, client.getUsername());
            userStmt.setString(3, client.getPasswordHash());
            userStmt.setString(4, client.getFirstName());
            userStmt.setString(5, client.getLastName());
            userStmt.setString(6, client.getPhoneNumber());
            userStmt.setBoolean(7, client.isEmailVerified());
            userStmt.setString(8, "CLIENT");
            userStmt.setInt(9, client.getId());
            userStmt.executeUpdate();
            userStmt.close();
            
            // Update client
            PreparedStatement clientStmt = conn.prepareStatement(updateClient);
            clientStmt.setString(1, client.getAddress());
            clientStmt.setString(2, client.getCity());
            clientStmt.setInt(3, client.getPostalCode());
            clientStmt.setInt(4, client.getId());
            clientStmt.executeUpdate();
            clientStmt.close();
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error updating client: " + e.getMessage());
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
        String deleteClient = "DELETE FROM client WHERE id=?";
        String deleteUser = "DELETE FROM users WHERE id=?";
        
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            PreparedStatement clientStmt = conn.prepareStatement(deleteClient);
            clientStmt.setInt(1, id);
            clientStmt.executeUpdate();
            clientStmt.close();
            
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
            System.err.println("Error deleting client: " + e.getMessage());
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
    public Client findById(int id) throws SQLException {
        String sql = "SELECT u.*, c.* FROM users u JOIN client c ON u.id = c.id WHERE u.id=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding client by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Client> findAll() throws SQLException {
        List<Client> clients = new ArrayList<>();
        String sql = "SELECT u.*, c.* FROM users u JOIN client c ON u.id = c.id";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clients.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all clients: " + e.getMessage());
            throw e;
        }
        
        return clients;
    }

    // Additional method
    public Client findByUserId(int userId) throws SQLException {
        return findById(userId);
    }
}
