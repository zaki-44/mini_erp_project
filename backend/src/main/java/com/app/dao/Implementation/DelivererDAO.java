package com.app.dao.Implementation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.app.dao.Interface.DAO;
import com.app.model.Affectation;
import com.app.model.Deliverer;
import com.app.model.Enums.VehicleType;
import com.app.util.Database;

//Tested
public class DelivererDAO implements DAO<Deliverer>{
    @Override
    public void insert(Deliverer deliverer) throws SQLException {
        // Add to deliverer and users table
        String toUser = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String toDeliverer = "INSERT INTO deliverer (id, vehicle_type, is_available, max_weight , current_load, city , serial_number , rate) "
                   + "VALUES (?, ?, ?, ? , ?, ?, ?, ?)";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);  // Start transaction so both inserts succeed or fail together

            PreparedStatement userStmt = conn.prepareStatement(toUser, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, deliverer.getEmail());
            userStmt.setString(2, deliverer.getUsername());
            userStmt.setString(3, deliverer.getPasswordHash());
            userStmt.setString(4, deliverer.getFirstName());
            userStmt.setString(5, deliverer.getLastName());
            userStmt.setString(6, deliverer.getPhoneNumber());
            userStmt.setString(7, "DELIVERER");
            userStmt.executeUpdate();

            // Get generated user ID
            ResultSet rs = userStmt.getGeneratedKeys();
            int userId = 0;
            if (rs.next()) {
                userId = rs.getInt(1);
            }
            rs.close();
            userStmt.close();
            deliverer.setId(userId); 
            // Insert into deliverers table
            PreparedStatement delivererStmt = conn.prepareStatement(toDeliverer);
            delivererStmt.setInt(1, userId);
            delivererStmt.setString(2, deliverer.getVehicleType().name());
            delivererStmt.setBoolean(3, deliverer.isAvailable());
            delivererStmt.setDouble(4, deliverer.getMaxWeight());
            delivererStmt.setDouble(5, deliverer.getCurrentLoad());
            delivererStmt.setString(6, deliverer.getCity());
            delivererStmt.setString(7, deliverer.getSerialNumber());
            delivererStmt.setDouble(8, deliverer.getRate());
            delivererStmt.executeUpdate();
            delivererStmt.close();

            conn.commit(); // Commit transaction
        }
        catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error inserting deliverer: " + e.getMessage());
            throw e;
        }
        finally {
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
        String updateUser = "UPDATE users SET email=?, username=?, password_hash=?, first_name=?, last_name=?, phone_number=?, role=?"
                   + "WHERE id=?";
        String updateDeliverer = "UPDATE deliverer SET vehicle_type=?, is_available=?, max_weight=?, current_load=?, city=?, serial_number=?, rate=? WHERE id=?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);  // Start transaction so both updates succeed or fail together
            PreparedStatement userStmt = conn.prepareStatement(updateUser);
            userStmt.setString(1, deliverer.getEmail());
            userStmt.setString(2, deliverer.getUsername());
            userStmt.setString(3, deliverer.getPasswordHash());
            userStmt.setString(4, deliverer.getFirstName());
            userStmt.setString(5, deliverer.getLastName());
            userStmt.setString(6, deliverer.getPhoneNumber());
            userStmt.setString(7, "DELIVERER");
            userStmt.setInt(8, deliverer.getId());
            
            userStmt.executeUpdate();
            userStmt.close();

            PreparedStatement delivererStmt = conn.prepareStatement(updateDeliverer);
            delivererStmt.setString(1, deliverer.getVehicleType().name());
            delivererStmt.setBoolean(2, deliverer.isAvailable());
            delivererStmt.setDouble(3, deliverer.getMaxWeight());
            delivererStmt.setDouble(4, deliverer.getCurrentLoad());
            delivererStmt.setString(5, deliverer.getCity());
            delivererStmt.setString(6, deliverer.getSerialNumber());
            
            delivererStmt.setDouble(7, deliverer.getRate());
            delivererStmt.setInt(8, deliverer.getId());
            delivererStmt.executeUpdate();
            delivererStmt.close();
            conn.commit(); // Commit transaction
        }
        catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error updating deliverer: " + e.getMessage());
            throw e;
        }
        finally {
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
        //Walid : You can delete only the user and the deliverer will be deleted 
        // automatically because of foreign key with cascade delete
        String deleteDeliverer = "DELETE FROM deliverer WHERE id=?";
        String deleteUser = "DELETE FROM users WHERE id=?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);  // Start transaction so both deletes succeed or fail together
            PreparedStatement delivererStmt = conn.prepareStatement(deleteDeliverer);
            delivererStmt.setInt(1, id);
            delivererStmt.executeUpdate();
            delivererStmt.close();

            PreparedStatement userStmt = conn.prepareStatement(deleteUser);
            userStmt.setInt(1, id);
            userStmt.executeUpdate();
            userStmt.close();
            conn.commit();
        }
        catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error deleting deliverer: " + e.getMessage());
            throw e;
        }
        finally {
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
        String sql = "SELECT u.*, d.* "
                   + "FROM users u JOIN deliverer d ON u.id = d.id WHERE u.id=?";
        Deliverer deliverer = null;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    deliverer = mapDeliverer(rs);
                    rs.close();
                }
            }
            catch(SQLException e){
                System.out.println("Error finding deliverer by ID: " + e.getMessage());
                throw e;
            }
        }
        return deliverer;
    }
    @Override
    public List<Deliverer> findAll() throws SQLException {
        String sql = "SELECT u.*, d.* "
                   + "FROM users u JOIN deliverer d ON u.id = d.id";
        List<Deliverer> deliverers = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Deliverer deliverer = mapDeliverer(rs);
                deliverers.add(deliverer);
            }
        }
        catch(SQLException e){
            System.out.println("Error finding all deliverers: " + e.getMessage());
            throw e;
        }
        return deliverers;
    }

    private Deliverer mapDeliverer(ResultSet rs) throws SQLException {
        try{
            Deliverer deliverer = new Deliverer();
            deliverer.setId(rs.getInt("id"));
            deliverer.setEmail(rs.getString("email"));
            deliverer.setUsername(rs.getString("username"));
            deliverer.setPasswordHash(rs.getString("password_hash"));
            deliverer.setFirstName(rs.getString("first_name"));
            deliverer.setLastName(rs.getString("last_name"));
            deliverer.setPhoneNumber(rs.getString("phone_number"));
            deliverer.setVehicleType(VehicleType.valueOf(rs.getString("vehicle_type")));
            deliverer.setAvailable(rs.getBoolean("is_available"));
            deliverer.setMaxWeight(rs.getDouble("max_weight"));
            deliverer.setCurrentLoad(rs.getDouble("current_load"));
            deliverer.setCity(rs.getString("city"));
            deliverer.setSerialNumber(rs.getString("serial_number"));
            deliverer.setRate(rs.getDouble("rate"));
            return deliverer;
        }
        catch(SQLException e){
            System.out.println("Error mapping deliverer: " + e.getMessage());
            throw e;
        }
    }

    public List<Deliverer> findAllAvailable() throws SQLException {
        String sql = "SELECT u.*, d.* "
                   + "FROM users u JOIN deliverer d ON u.id = d.id WHERE d.is_available = TRUE";
        List<Deliverer> deliverers = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Deliverer deliverer = mapDeliverer(rs);
                deliverers.add(deliverer);
            }
        }
        catch(SQLException e){
            System.out.println("Error finding all available deliverers: " + e.getMessage());
            throw e;
        }
        return deliverers;
    }

    public List<Deliverer> findByAffectation(Affectation aff) throws SQLException {
        String sql = "SELECT u.*, d.* "
                   + "FROM users u JOIN deliverer d ON u.id = d.id "
                   + "JOIN affectation a ON d.id = a.id_deliverer WHERE a.id_affectation=?";
        List<Deliverer> deliverers = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, aff.getIdAffectation());
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Deliverer deliverer = mapDeliverer(rs);
                    deliverers.add(deliverer);
                }
            }
        }
        catch(SQLException e){
            System.out.println("Error finding deliverers by affectation: " + e.getMessage());
            throw e;
        }
        return deliverers;
    }
    public List<Deliverer> findAvailableByWeight(double packageWeight) throws SQLException {
        // Look for deliverers who are manually 'available' AND have room for the weight
        String sql = "SELECT u.*, d.* FROM users u JOIN deliverer d ON u.id = d.id " +
                    "WHERE d.is_available = TRUE AND (d.current_load + ?) <= d.max_weight";
        
        List<Deliverer> deliverers = new ArrayList<>();
        try (Connection conn = Database.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, packageWeight);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                deliverers.add(mapDeliverer(rs));
            }
        }
        return deliverers;
    }   

}