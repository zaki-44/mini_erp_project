package com.app.dao.Implementation;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.app.dao.Interface.DAO;
import com.app.model.Client;
import com.app.util.Database;

//Tested

public class ClientDAO implements DAO<Client> {

    @Override
    public void insert(Client client) throws SQLException {
        String insertUser = "INSERT INTO users (email, username, password_hash, first_name, last_name, phone_number, role) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        String insertClient = "INSERT INTO client (id, address) VALUES (?, ?)";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement userStmt = conn.prepareStatement(insertUser, Statement.RETURN_GENERATED_KEYS);
            userStmt.setString(1, client.getEmail());
            userStmt.setString(2, client.getUsername());
            userStmt.setString(3, client.getPasswordHash());
            userStmt.setString(4, client.getFirstName());
            userStmt.setString(5, client.getLastName());
            userStmt.setString(6, client.getPhoneNumber());
            userStmt.setString(7, "CLIENT");
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

            // Insert into client
            PreparedStatement clientStmt = conn.prepareStatement(insertClient);
            clientStmt.setInt(1, userId);
            clientStmt.setString(2, client.getAddress());
            clientStmt.executeUpdate();
            clientStmt.close();
            conn.commit();

        } catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error inserting client: " + e.getMessage());
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
    public void update(Client client) throws SQLException {
        String updateUser = "UPDATE users SET email=?, username=?, password_hash=?, first_name=?, last_name=?, phone_number=?, role=? "
                + "WHERE id=?";

        String updateClient = "UPDATE client SET address=? WHERE id=?";
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);

            PreparedStatement userStmt = conn.prepareStatement(updateUser);
            userStmt.setString(1, client.getEmail());
            userStmt.setString(2, client.getUsername());
            userStmt.setString(3, client.getPasswordHash());
            userStmt.setString(4, client.getFirstName());
            userStmt.setString(5, client.getLastName());
            userStmt.setString(6, client.getPhoneNumber());
            userStmt.setString(7, "CLIENT");
            userStmt.setInt(8, client.getId());
            userStmt.executeUpdate();
            userStmt.close();

            PreparedStatement clientStmt = conn.prepareStatement(updateClient);
            clientStmt.setString(1, client.getAddress());
            clientStmt.setInt(2, client.getId());
            clientStmt.executeUpdate();
            clientStmt.close();
            conn.commit();

        } catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error updating client: " + e.getMessage());
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
        //Walid : You can delete only the user and the client will be deleted 
        // automatically because of foreign key with cascade delete
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

        } catch(SQLException e){
            if (conn != null) {
            try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            System.out.println("Error deleting client: " + e.getMessage());
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
    public Client findById(int id) throws SQLException {
        String sql = "SELECT u.id, u.email, u.username, u.password_hash, u.first_name, u.last_name, u.phone_number, c.address "
                   + "FROM users u JOIN client c ON u.id = c.id WHERE u.id=?";

        Client client = null;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    client = new Client();
                    client.setId(rs.getInt("id"));
                    client.setEmail(rs.getString("email"));
                    client.setUsername(rs.getString("username"));
                    client.setPasswordHash(rs.getString("password_hash"));
                    client.setFirstName(rs.getString("first_name"));
                    client.setLastName(rs.getString("last_name"));
                    client.setPhoneNumber(rs.getString("phone_number"));
                    client.setAddress(rs.getString("address"));
                }
            }

        } catch (SQLException e) {
            System.out.println("Error finding client by ID: " + e.getMessage());
            throw e;
        }

        return client;
    }

    @Override
    public List<Client> findAll() throws SQLException {
        String sql = "SELECT u.id, u.email, u.username, u.password_hash, u.first_name, u.last_name, u.phone_number, c.address "
                   + "FROM users u JOIN client c ON u.id = c.id";

        List<Client> clients = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Client client = new Client();
                client.setId(rs.getInt("id"));
                client.setEmail(rs.getString("email"));
                client.setUsername(rs.getString("username"));
                client.setPasswordHash(rs.getString("password_hash"));
                client.setFirstName(rs.getString("first_name"));
                client.setLastName(rs.getString("last_name"));
                client.setPhoneNumber(rs.getString("phone_number"));
                client.setAddress(rs.getString("address"));
                clients.add(client);
            }

        } catch (SQLException e) {
            System.out.println("Error finding all clients: " + e.getMessage());
            throw e;
        }

        return clients;
    }
}
