package com.app.dao.implementation.verification;

import com.app.model.verification.VerificationCode;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VerificationCodeDAO {

    private VerificationCode mapResultSetToEntity(ResultSet rs) throws SQLException {
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(rs.getString("email"));
        verificationCode.setCode(rs.getString("code"));
        verificationCode.setCreatedAt(rs.getTimestamp("created_at"));
        verificationCode.setExpiresAt(rs.getTimestamp("expires_at"));
        return verificationCode;
    }

    // Atomic operation with provided connection
    public void insert(Connection conn, VerificationCode verificationCode) throws SQLException {
        String sql = "INSERT INTO verification_code (email, code, expires_at) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, verificationCode.getEmail());
            stmt.setString(2, verificationCode.getCode());
            stmt.setTimestamp(3, verificationCode.getExpiresAt());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error inserting verification code: " + e.getMessage());
            throw e;
        }
    }

    public void insert(VerificationCode verificationCode) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            insert(conn, verificationCode);
        }
    }

    // Atomic operation with provided connection
    public void update(Connection conn, VerificationCode verificationCode) throws SQLException {
        String sql = "UPDATE verification_code SET code=?, expires_at=? WHERE email=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, verificationCode.getCode());
            stmt.setTimestamp(2, verificationCode.getExpiresAt());
            stmt.setString(3, verificationCode.getEmail());
            
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating verification code: " + e.getMessage());
            throw e;
        }
    }

    public void update(VerificationCode verificationCode) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            update(conn, verificationCode);
        }
    }

    // Atomic operation with provided connection
    public void delete(Connection conn, String email) throws SQLException {
        String sql = "DELETE FROM verification_code WHERE email=?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting verification code: " + e.getMessage());
            throw e;
        }
    }

    public void delete(String email) throws SQLException {
        try (Connection conn = Database.getConnection()) {
            delete(conn, email);
        }
    }

    public VerificationCode findByEmail(String email) throws SQLException {
        String sql = "SELECT * FROM verification_code WHERE email=?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, email);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToEntity(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding verification code by email: " + e.getMessage());
            throw e;
        }
        
        return null;
    }

    public List<VerificationCode> findAll() throws SQLException {
        List<VerificationCode> verificationCodes = new ArrayList<>();
        String sql = "SELECT * FROM verification_code";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                verificationCodes.add(mapResultSetToEntity(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all verification codes: " + e.getMessage());
            throw e;
        }
        
        return verificationCodes;
    }

    public void deleteExpiredCodes() throws SQLException {
        String sql = "DELETE FROM verification_code WHERE expires_at < NOW()";
        
        try (Connection conn = Database.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("Error deleting expired verification codes: " + e.getMessage());
            throw e;
        }
    }
}
