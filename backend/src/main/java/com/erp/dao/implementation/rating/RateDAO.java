package com.erp.dao.implementation.rating;

import com.erp.dao.interfaces.GenericDAO;
import com.erp.model.rating.Rate;
import com.erp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RateDAO implements GenericDAO<Rate> {

    private Rate mapResultSetToEntity(ResultSet rs) throws SQLException {
        Rate rate = new Rate();
        rate.setId(rs.getInt("id"));
        rate.setClientId(rs.getInt("id_client"));
        rate.setDelivererId(rs.getInt("id_deliverer"));
        rate.setScore(rs.getFloat("score"));
        rate.setComment(rs.getString("comment"));
        rate.setCreatedAt(rs.getTimestamp("created_at"));
        return rate;
    }

    @Override
    public void insert(Rate rate) throws SQLException {
        String sql = "INSERT INTO rate (id_client, id_deliverer, score, comment) VALUES (?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setInt(1, rate.getClientId());
            stmt.setInt(2, rate.getDelivererId());
            stmt.setFloat(3, rate.getScore());
            stmt.setString(4, rate.getComment());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    rate.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting rate: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Rate rate) throws SQLException {
        String sql = "UPDATE rate SET id_client=?, id_deliverer=?, score=?, comment=? WHERE id=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, rate.getClientId());
            stmt.setInt(2, rate.getDelivererId());
            stmt.setFloat(3, rate.getScore());
            stmt.setString(4, rate.getComment());
            stmt.setInt(5, rate.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating rate: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM rate WHERE id=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting rate: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Rate findById(int id) throws SQLException {
        String sql = "SELECT * FROM rate WHERE id=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding rate by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Rate> findAll() throws SQLException {
        List<Rate> rates = new ArrayList<>();
        String sql = "SELECT * FROM rate";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                rates.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all rates: " + e.getMessage());
            throw e;
        }
        
        return rates;
    }

    // Additional methods
    public List<Rate> findByDeliverer(int delivererId) throws SQLException {
        List<Rate> rates = new ArrayList<>();
        String sql = "SELECT * FROM rate WHERE id_deliverer=? ORDER BY created_at DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, delivererId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rates.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding rates by deliverer: " + e.getMessage());
            throw e;
        }
        
        return rates;
    }

    public List<Rate> findByClient(int clientId) throws SQLException {
        List<Rate> rates = new ArrayList<>();
        String sql = "SELECT * FROM rate WHERE id_client=? ORDER BY created_at DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    rates.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding rates by client: " + e.getMessage());
            throw e;
        }
        
        return rates;
    }

    public float getAverageRateForDeliverer(int delivererId) throws SQLException {
        String sql = "SELECT AVG(score) as average FROM rate WHERE id_deliverer=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, delivererId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getFloat("average");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rate for deliverer: " + e.getMessage());
            throw e;
        }
        
        return 0.0f;
    }
}
