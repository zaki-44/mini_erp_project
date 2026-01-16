package com.app.service;

import com.app.dao.implementation.users.ClientDAO;
import com.app.dao.implementation.verification.VerificationCodeDAO;
import com.app.model.users.Client;
import com.app.model.verification.VerificationCode;
import com.app.util.Database;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class ClientService {
    private ClientDAO clientDAO;
    private VerificationCodeDAO verificationCodeDAO;
    
    public ClientService() {
        this.clientDAO = new ClientDAO();
        this.verificationCodeDAO = new VerificationCodeDAO();
    }
    
    public Client registerClient(Client client, String verificationCode, Timestamp expiresAt) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            clientDAO.insert(conn, client);
            
            VerificationCode vc = new VerificationCode();
            vc.setEmail(client.getEmail());
            vc.setCode(verificationCode);
            vc.setExpiresAt(expiresAt);
            verificationCodeDAO.insert(conn, vc);
            
            conn.commit();
            return client;
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
    
    public Client findById(int id) throws SQLException {
        return clientDAO.findById(id);
    }
    
    public Client findByUserId(int userId) throws SQLException {
        return clientDAO.findByUserId(userId);
    }
    
    public List<Client> getAllClients() throws SQLException {
        return clientDAO.findAll();
    }
    
    public void updateClient(Client client) throws SQLException {
        clientDAO.update(client);
    }
    
    public void deleteClient(int id) throws SQLException {
        clientDAO.delete(id);
    }
}
