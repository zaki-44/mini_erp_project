package com.app.dao.Implementation;

import com.app.dao.Interface.DAO;
import com.app.model.Rate;
import com.app.util.Database;

import java.util.List;
import java.sql.*;

public class RateDAO implements DAO<Rate> {
    public void setStatementParameters(PreparedStatement pstmt, Rate rate) throws SQLException {
        pstmt.setInt(1, rate.getIdDeliverer());
        pstmt.setInt(2, rate.getIdClient());
        pstmt.setDouble(3, rate.getRating());
        pstmt.setString(4, rate.getComment());
        pstmt.setDate(5, rate.getCreatedAt());
    }
    public Rate mapResultSetToRate(ResultSet rs) throws SQLException {
        int idDeliverer = rs.getInt("id_deliverer");
        int idClient = rs.getInt("id_client");
        double rating = rs.getDouble("rating");
        String comment = rs.getString("comment");
        Date createdAt = rs.getDate("created_at");
        int id = rs.getInt("id_rate");
        return new Rate(id, idDeliverer, idClient, rating, comment, createdAt);
    }

    public void insert(Connection conn, Rate rate) {
        String sql = "INSERT INTO rate (id_deliverer, id_client, rating, comment, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            setStatementParameters(pstmt, rate);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error inserting rate: " + e.getMessage());
        }
    }
    @Override
    public void insert(Rate rate) {
        try{
            Connection conn = Database.getConnection();
            insert(conn, rate);
        } catch (SQLException e) {
            System.out.println("Error obtaining database connection: " + e.getMessage());
        }
    }
    public void update(Connection conn, Rate rate) {
        String sql = "UPDATE rate SET rating = ?, comment = ?, created_at = ? WHERE id_deliverer = ? AND id_client = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, rate.getRating());
            pstmt.setString(2, rate.getComment());
            pstmt.setDate(3, rate.getCreatedAt());
            pstmt.setInt(4, rate.getIdDeliverer());
            pstmt.setInt(5, rate.getIdClient());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating rate: " + e.getMessage());
        }
    }
    @Override
    public void update(Rate rate) {
        try{
            Connection conn = Database.getConnection();
            update(conn, rate);
        } catch (SQLException e) {
            System.out.println("Error obtaining database connection: " + e.getMessage());
        }
    }
    public void delete(Connection conn, int id) {
        String sql = "DELETE FROM rate WHERE id_rate = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error deleting rate: " + e.getMessage());
        }
    }
    @Override
    public void delete(int id) {
        try{
            Connection conn = Database.getConnection();
            delete(conn, id);
        } catch (SQLException e) {
            System.out.println("Error obtaining database connection: " + e.getMessage());
        }
    }
    @Override
    public Rate findById(int id) {
        String sql = "SELECT * FROM rate WHERE id_rate = ?";
        try (Connection conn = Database.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return mapResultSetToRate(rs);
                }
            } catch (SQLException e) {
                System.out.println("Error finding rate by ID: " + e.getMessage());
            }
        return null;
    }
    @Override
    public List<Rate> findAll() {
        String sql = "SELECT * FROM rate";
        List<Rate> rates = new java.util.ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                rates.add(mapResultSetToRate(rs));
            }
        } catch (SQLException e) {
            System.out.println("Error finding all rates: " + e.getMessage());
        }
        return rates;
    }
}
