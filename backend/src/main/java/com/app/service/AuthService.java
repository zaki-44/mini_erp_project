package com.app.service;

import com.app.dao.implementation.users.UserDAO;
import com.app.dao.implementation.verification.VerificationCodeDAO;
import com.app.model.users.User;
import com.app.model.verification.VerificationCode;
import com.app.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;

public class AuthService {
    private UserDAO userDAO;
    private VerificationCodeDAO verificationCodeDAO;
    
    public AuthService() {
        this.userDAO = new UserDAO();
        this.verificationCodeDAO = new VerificationCodeDAO();
    }
    
    public User registerUser(User user, String verificationCode, Timestamp expiresAt) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            userDAO.insert(conn, user);
            
            VerificationCode vc = new VerificationCode();
            vc.setEmail(user.getEmail());
            vc.setCode(verificationCode);
            vc.setExpiresAt(expiresAt);
            verificationCodeDAO.insert(conn, vc);
            
            conn.commit();
            return user;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            Database.closeConnection(conn);
        }
    }
    
    public boolean verifyEmail(String email, String code) throws SQLException {
        VerificationCode vc = verificationCodeDAO.findByEmail(email);
        
        if (vc == null) {
            return false;
        }
        
        if (!vc.getCode().equals(code)) {
            return false;
        }
        
        if (vc.getExpiresAt().before(new Timestamp(System.currentTimeMillis()))) {
            return false;
        }
        
        User user = userDAO.findByEmail(email);
        if (user != null) {
            user.setEmailVerified(true);
            userDAO.update(user);
            verificationCodeDAO.delete(email);
            return true;
        }
        
        return false;
    }
    
    public User login(String email, String passwordHash) throws SQLException {
        User user = userDAO.findByEmail(email);
        
        if (user == null) {
            return null;
        }
        
        if (!user.getPasswordHash().equals(passwordHash)) {
            return null;
        }
        
        if (!user.isEmailVerified()) {
            return null;
        }
        
        return user;
    }
    
    public void resendVerificationCode(String email, String code, Timestamp expiresAt) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            VerificationCode existingCode = verificationCodeDAO.findByEmail(email);
            if(existingCode != null) {
                existingCode.setCode(code);
                existingCode.setExpiresAt(expiresAt);
                verificationCodeDAO.update(conn, existingCode);
            } else {
                VerificationCode newCode = new VerificationCode();
                newCode.setEmail(email);
                newCode.setCode(code);
                newCode.setExpiresAt(expiresAt);
                verificationCodeDAO.insert(conn, newCode);
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Failed to rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        } finally {
            Database.closeConnection(conn);
        }
    }
}
