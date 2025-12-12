package com.app.dao.Implementation;

import java.sql.*;
import java.util.List;

import com.app.dao.Interface.DAO;
import com.app.model.Deliverer;
import com.app.util.Database;

//Tested

public class DelivererDAO implements DAO<Deliverer>{
    @Override
    public void insert(Deliverer deliverer) throws SQLException {
        // Add to deliverer and users table
        String toUser = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        String toDeliverer = "INSERT INTO deliverer (id, vehicle_type, is_available) "
                   + "VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection()){
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
            delivererStmt.setString(2, deliverer.getVehicleType());
            delivererStmt.setBoolean(3, deliverer.isAvailable());
            delivererStmt.executeUpdate();
            delivererStmt.close();
        }
        catch(SQLException e){
            System.out.println("Error inserting deliverer: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void update(Deliverer deliverer) throws SQLException {
        String updateUser = "UPDATE users SET email=?, username=?, password_hash=?, first_name=?, last_name=?, phone_number=?, role=? "
                   + "WHERE id=?";
        String updateDeliverer = "UPDATE deliverer SET vehicle_type=?, is_available=? WHERE id=?";
        try (Connection conn = Database.getConnection()){
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
            delivererStmt.setString(1, deliverer.getVehicleType());
            delivererStmt.setBoolean(2, deliverer.isAvailable());
            delivererStmt.setInt(3, deliverer.getId());
            delivererStmt.executeUpdate();
            delivererStmt.close();
        }
        catch(SQLException e){
            System.out.println("Error updating deliverer: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public void delete(int id) throws SQLException {
        String deleteDeliverer = "DELETE FROM deliverer WHERE id=?";
        String deleteUser = "DELETE FROM users WHERE id=?";
        try (Connection conn = Database.getConnection()){
            PreparedStatement delivererStmt = conn.prepareStatement(deleteDeliverer);
            delivererStmt.setInt(1, id);
            delivererStmt.executeUpdate();
            delivererStmt.close();

            PreparedStatement userStmt = conn.prepareStatement(deleteUser);
            userStmt.setInt(1, id);
            userStmt.executeUpdate();
            userStmt.close();
        }
        catch(SQLException e){
            System.out.println("Error deleting deliverer: " + e.getMessage());
            throw e;
        }
    }
    @Override
    public Deliverer findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.email, u.username, u.password_hash, u.first_name, u.last_name, u.phone_number, d.vehicle_type , d.is_available "
                   + "FROM users u JOIN deliverer d ON u.id = d.id WHERE u.id=?";
        Deliverer deliverer = null;
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    deliverer = new Deliverer();
                    deliverer.setId(rs.getInt("id"));
                    deliverer.setEmail(rs.getString("email"));
                    deliverer.setUsername(rs.getString("username"));
                    deliverer.setPasswordHash(rs.getString("password_hash"));
                    deliverer.setFirstName(rs.getString("first_name"));
                    deliverer.setLastName(rs.getString("last_name"));
                    deliverer.setPhoneNumber(rs.getString("phone_number"));
                    deliverer.setVehicleType(rs.getString("vehicle_type"));
                    deliverer.setAvailable(rs.getBoolean("is_available"));
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
        String sql = "SELECT u.id, u.email, u.username, u.password_hash, u.first_name, u.last_name, u.phone_number, d.vehicle_type , d.is_available "
                   + "FROM users u JOIN deliverer d ON u.id = d.id";
        List<Deliverer> deliverers = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Deliverer deliverer = new Deliverer();
                deliverer.setId(rs.getInt("id"));
                deliverer.setEmail(rs.getString("email"));
                deliverer.setUsername(rs.getString("username"));
                deliverer.setPasswordHash(rs.getString("password_hash"));
                deliverer.setFirstName(rs.getString("first_name"));
                deliverer.setLastName(rs.getString("last_name"));
                deliverer.setPhoneNumber(rs.getString("phone_number"));
                deliverer.setVehicleType(rs.getString("vehicle_type"));
                deliverer.setAvailable(rs.getBoolean("is_available"));
                deliverers.add(deliverer);
            }
        }
        catch(SQLException e){
            System.out.println("Error finding all deliverers: " + e.getMessage());
            throw e;
        }
        return deliverers;
    }

}