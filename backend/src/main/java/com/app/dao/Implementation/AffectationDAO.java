package com.app.dao.Implementation;

import com.app.dao.Interface.DAO;
import com.app.model.Affectation;
import com.app.model.Enums.AffectationStatus;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//Tested

public class AffectationDAO implements DAO<Affectation> {

    @Override
    public void insert(Affectation aff) throws SQLException {
        String sql = "INSERT INTO affectation (id_deliverer, id_package, status, assigned_at) "
                   + "VALUES (?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, aff.getIdDeliverer());
            stmt.setInt(2, aff.getIdPackage());
            stmt.setString(3, aff.getStatus().name());
            stmt.setTimestamp(4, aff.getAssignedAt());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    aff.setIdAffectation(rs.getInt(1));
                }
            }
        }
        catch(SQLException e) {
            System.out.println("Error inserting Affectation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Affectation aff) throws SQLException {
        String sql = "UPDATE affectation SET id_deliverer=?, id_package=?, status=?, assigned_at=? "
                   + "WHERE id_affectation=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, aff.getIdDeliverer());
            stmt.setInt(2, aff.getIdPackage());
            stmt.setString(3, aff.getStatus().name());
            stmt.setTimestamp(4, aff.getAssignedAt());
            stmt.setInt(5, aff.getIdAffectation());

            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println("Error updating Affectation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM affectation WHERE id_affectation=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println("Error deleting Affectation: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Affectation findById(int id) throws SQLException {
        String sql = "SELECT * FROM affectation WHERE id_affectation=?";
        Affectation aff = null;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    aff = mapAffectation(rs);
                }
            }
        }
        catch(SQLException e) {
            System.out.println("Error finding Affectation by ID: " + e.getMessage());
            throw e;
        }

        return aff;
    }

    @Override
    public List<Affectation> findAll() throws SQLException {
        String sql = "SELECT * FROM affectation";
        List<Affectation> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapAffectation(rs));
            }
        }
        catch(SQLException e) {
            System.out.println("Error finding all Affectations: " + e.getMessage());
            throw e;
        }

        return list;
    }

    public List<Affectation> findByDelivererId(int delivererId) throws SQLException {
        String sql = "SELECT * FROM affectation WHERE id_deliverer=?";
        List<Affectation> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, delivererId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapAffectation(rs));
                }
            }
        }
        catch(SQLException e) {
            System.out.println("Error finding Affectations by Deliverer ID: " + e.getMessage());
            throw e;
        }

        return list;
    }
    
    private Affectation mapAffectation(ResultSet rs) throws SQLException {
        Affectation a = new Affectation();

        a.setIdAffectation(rs.getInt("id_affectation"));
        a.setIdDeliverer(rs.getInt("id_deliverer"));
        a.setIdPackage(rs.getInt("id_package"));
        a.setStatus(AffectationStatus.valueOf(rs.getString("status")));
        a.setAssignedAt(rs.getTimestamp("assigned_at"));

        return a;
    }

    
    
}
