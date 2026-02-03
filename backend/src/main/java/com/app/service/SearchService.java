package com.app.service;

import com.app.dao.Implementation.ClientDAO;
import com.app.model.Client;
import java.util.List;
public class SearchService {
    // private UserDAO userDAO = new UserDAO();
    private ClientDAO clientDAO = new ClientDAO();
    public List<Client> searchUsersByName(String nameQuery) throws Exception {
        try {
            return clientDAO.searchByName(nameQuery);
        } catch (Exception e) {
            System.out.println("Error searching users: " + e.getMessage());
            throw e;
        }
    }
}
