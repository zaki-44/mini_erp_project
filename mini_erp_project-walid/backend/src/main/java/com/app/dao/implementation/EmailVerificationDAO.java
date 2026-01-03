package com.app.dao.Implementation;
import java.sql.*;
import com.app.util.Database;



public class EmailVerificationDAO{
    public void saveVerificationCode(int userId, String code) throws SQLException {
        String insertCode = "INSERT INTO email_verification (user_id, verification_code_hash, created_at , expires_at) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertCode)) {
            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(4, new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 minutes expiry
            stmt.executeUpdate();
        }
    }
    public boolean verifyCode(int userId, String code) throws SQLException {
        String query = "SELECT * FROM email_verification WHERE user_id = ? AND verification_code_hash = ? AND expires_at > ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            stmt.setString(2, code);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }
    public void markAsVerified(int userId) throws SQLException {
        String deleteCode = "DELETE FROM email_verification WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(deleteCode)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    public void updateVerificationCode(int userId, String newCode) throws SQLException {
        String updateCode = "UPDATE email_verification SET verification_code_hash = ?, created_at = ?, expires_at = ? WHERE user_id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(updateCode)) {
            stmt.setString(1, newCode);
            stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis() + 15 * 60 * 1000)); // 15 minutes expiry
            stmt.setInt(4, userId);
            stmt.executeUpdate();
        }
    }
}
