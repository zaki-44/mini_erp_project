package com.app.dao.interfaces;

import java.sql.SQLException;
import java.util.List;

public interface DAO<T> {
    void insert(T entity) throws SQLException;
    void update(T entity) throws SQLException;
    void delete(int id) throws SQLException;
    T findById(int id) throws SQLException;
    List<T> findAll() throws SQLException;
}
