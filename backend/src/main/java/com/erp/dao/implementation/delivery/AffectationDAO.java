package com.erp.dao.implementation.delivery;

import com.erp.dao.interfaces.GenericDAO;
import com.erp.model.delivery.Affectation;
import com.erp.model.enums.AffectationStatus;
import com.erp.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AffectationDAO implements GenericDAO<Affectation> {

    private Affectation mapResultSetToEntity(ResultSet rs) throws SQLException {
        Affectation affectation = new Affectation();
        affectation.setId(rs.getInt("id_affectation"));
        affectation.setDelivererId(rs.getInt("id_deliverer"));
        affectation.setPackageId(rs.getInt("id_package"));
        affectation.setStatus(AffectationStatus.valueOf(rs.getString("status")));
        affectation.setAssignedAt(rs.getTimestamp("assigned_at"));
        return affectation;
    }

    // Atomic operation with provided connection
    public void insert(Connection conn, Affectation affectation) throws SQLException {
        String sql = "INSERT INTO affectation (id_deliverer, id_package, status) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, affectation.getDelivererId());
            stmt.setInt(2, affectation.getPackageId());
            stmt.setString(3, affectation.getStatus() != null ? affectation.getStatus().name() : AffectationStatus.PENDING.name());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    affectation.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting affectation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Affectation affectation) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            insert(conn, affectation);
        }
    }

    // Atomic operation with provided connection
    public void update(Connection conn, Affectation affectation) throws SQLException {
        String sql = "UPDATE affectation SET id_deliverer=?, id_package=?, status=? WHERE id_affectation=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, affectation.getDelivererId());
            stmt.setInt(2, affectation.getPackageId());
            stmt.setString(3, affectation.getStatus().name());
            stmt.setInt(4, affectation.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating affectation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Affectation affectation) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            update(conn, affectation);
        }
    }

    // Atomic operation with provided connection
    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM affectation WHERE id_affectation=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting affectation: " + e.getMessage());
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
    public Affectation findById(int id) throws SQLException {
        String sql = "SELECT * FROM affectation WHERE id_affectation=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding affectation by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Affectation> findAll() throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT * FROM affectation";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                affectations.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all affectations: " + e.getMessage());
            throw e;
        }
        
        return affectations;
    }

    // Additional methods
    public List<Affectation> findByDeliverer(int delivererId) throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT * FROM affectation WHERE id_deliverer=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, delivererId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding affectations by deliverer: " + e.getMessage());
            throw e;
        }
        
        return affectations;
    }

    public List<Affectation> findByPackage(int packageId) throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT * FROM affectation WHERE id_package=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, packageId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding affectations by package: " + e.getMessage());
            throw e;
        }
        
        return affectations;
    }

    public List<Affectation> findByStatus(String status) throws SQLException {
        List<Affectation> affectations = new ArrayList<>();
        String sql = "SELECT * FROM affectation WHERE status=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    affectations.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding affectations by status: " + e.getMessage());
            throw e;
        }
        
        return affectations;
    }
}
