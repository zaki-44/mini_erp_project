package com.app.dao.implementation.delivery;

import com.app.dao.interfaces.DAO;
import com.app.model.delivery.Package;
import com.app.model.enums.PackageStatus;
import com.app.model.enums.VehicleType;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PackageDAO implements DAO<Package> {

    private Package mapResultSetToEntity(ResultSet rs) throws SQLException {
        Package pkg = new Package();
        pkg.setId(rs.getInt("id_package"));
        pkg.setClientSourceId(rs.getInt("id_client_source"));
        pkg.setClientDestinationId(rs.getInt("id_client_destination"));
        
        String vehicleTypeStr = rs.getString("vehicle_type_needed");
        if (vehicleTypeStr != null && !vehicleTypeStr.isEmpty()) {
            pkg.setVehicleTypeNeeded(VehicleType.valueOf(vehicleTypeStr));
        }
        
        pkg.setAddressSource(rs.getString("address_source"));
        pkg.setAddressDestination(rs.getString("address_destination"));
        pkg.setWeight(rs.getFloat("weight"));
        pkg.setPrice(rs.getFloat("price"));
        pkg.setDimensions(rs.getString("dimensions"));
        pkg.setDescription(rs.getString("description"));
        pkg.setDeliveryInstructions(rs.getString("delivery_instructions"));
        pkg.setStatus(PackageStatus.valueOf(rs.getString("status")));
        pkg.setCreatedAt(rs.getTimestamp("created_at"));
        return pkg;
    }

    // Atomic operation with provided connection
    public void insert(Connection conn, Package pkg) throws SQLException {
        String sql = "INSERT INTO package (id_client_source, id_client_destination, vehicle_type_needed, address_source, " +
                     "address_destination, weight, price, dimensions, description, delivery_instructions, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, pkg.getClientSourceId());
            stmt.setInt(2, pkg.getClientDestinationId());
            stmt.setString(3, pkg.getVehicleTypeNeeded() != null ? pkg.getVehicleTypeNeeded().name() : null);
            stmt.setString(4, pkg.getAddressSource());
            stmt.setString(5, pkg.getAddressDestination());
            stmt.setFloat(6, pkg.getWeight());
            stmt.setFloat(7, pkg.getPrice());
            stmt.setString(8, pkg.getDimensions());
            stmt.setString(9, pkg.getDescription());
            stmt.setString(10, pkg.getDeliveryInstructions());
            stmt.setString(11, pkg.getStatus() != null ? pkg.getStatus().name() : PackageStatus.CREATED.name());
            
            stmt.executeUpdate();
            
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pkg.setId(rs.getInt(1));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error inserting package: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void insert(Package pkg) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            insert(conn, pkg);
        }
    }

    // Atomic operation with provided connection
    public void update(Connection conn, Package pkg) throws SQLException {
        String sql = "UPDATE package SET id_client_source=?, id_client_destination=?, vehicle_type_needed=?, " +
                     "address_source=?, address_destination=?, weight=?, price=?, dimensions=?, description=?, " +
                     "delivery_instructions=?, status=? WHERE id_package=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, pkg.getClientSourceId());
            stmt.setInt(2, pkg.getClientDestinationId());
            stmt.setString(3, pkg.getVehicleTypeNeeded() != null ? pkg.getVehicleTypeNeeded().name() : null);
            stmt.setString(4, pkg.getAddressSource());
            stmt.setString(5, pkg.getAddressDestination());
            stmt.setFloat(6, pkg.getWeight());
            stmt.setFloat(7, pkg.getPrice());
            stmt.setString(8, pkg.getDimensions());
            stmt.setString(9, pkg.getDescription());
            stmt.setString(10, pkg.getDeliveryInstructions());
            stmt.setString(11, pkg.getStatus().name());
            stmt.setInt(12, pkg.getId());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating package: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Package pkg) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            update(conn, pkg);
        }
    }

    // Atomic operation with provided connection
    public void delete(Connection conn, int id) throws SQLException {
        String sql = "DELETE FROM package WHERE id_package=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting package: " + e.getMessage());
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
    public Package findById(int id) throws SQLException {
        String sql = "SELECT * FROM package WHERE id_package=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding package by ID: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    @Override
    public List<Package> findAll() throws SQLException {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT * FROM package";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                packages.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all packages: " + e.getMessage());
            throw e;
        }
        
        return packages;
    }

    // Additional methods
    public List<Package> findByClientSource(int clientId) throws SQLException {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT * FROM package WHERE id_client_source=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    packages.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding packages by client source: " + e.getMessage());
            throw e;
        }
        
        return packages;
    }

    public List<Package> findByClientDestination(int clientId) throws SQLException {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT * FROM package WHERE id_client_destination=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, clientId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    packages.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding packages by client destination: " + e.getMessage());
            throw e;
        }
        
        return packages;
    }

    public List<Package> findByStatus(String status) throws SQLException {
        List<Package> packages = new ArrayList<>();
        String sql = "SELECT * FROM package WHERE status=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, status);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    packages.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding packages by status: " + e.getMessage());
            throw e;
        }
        
        return packages;
    }
}
