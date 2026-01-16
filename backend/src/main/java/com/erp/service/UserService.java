package com.erp.service;

import com.erp.dao.implementation.user.UserDAO;
import com.erp.dao.implementation.verification.VerificationCodeDAO;
import com.erp.model.user.User;
import com.erp.model.verification.VerificationCode;
import com.erp.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class UserService {
    private UserDAO userDAO;
    private VerificationCodeDAO verificationCodeDAO;
    
    public UserService() {
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
    
    public User findByEmail(String email) throws SQLException {
        return userDAO.findByEmail(email);
    }
    
    public User findByUsername(String username) throws SQLException {
        return userDAO.findByUsername(username);
    }
    
    public User findById(int id) throws SQLException {
        return userDAO.findById(id);
    }
    
    public List<User> findByRole(String role) throws SQLException {
        return userDAO.findByRole(role);
    }
    
    public void updateUser(User user) throws SQLException {
        userDAO.update(user);
    }
    
    public void deleteUser(int id) throws SQLException {
        userDAO.delete(id);
    }
    
    public List<User> getAllUsers() throws SQLException {
        return userDAO.findAll();
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
