package com.app.service;

import com.app.dao.implementation.users.UserDAO;
import com.app.model.users.User;

import java.sql.SQLException;
import java.util.List;

public class UserService {
    private UserDAO userDAO;
    
    public UserService() {
        this.userDAO = new UserDAO();
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
}
