package com.app.dao.implementation;

import com.app.dao.Interface.DAO;
import com.app.model.Notification;
import com.app.model.Enums.NotificationType;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


//Tested
public class NotificationDAO implements DAO<Notification> {

    @Override
    public void insert(Notification notif) throws SQLException {
        String sql = "INSERT INTO notification (id_package, id_user_target, message, type, is_read, date_notif) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, notif.getIdPackage());
            stmt.setInt(2, notif.getIdUserTarget());
            stmt.setString(3, notif.getMessage());
            stmt.setString(4, notif.getType().name());
            stmt.setBoolean(5, notif.isRead());
            stmt.setTimestamp(6, notif.getDateNotif());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    notif.setIdNotification(rs.getInt(1));
                }
            }
        }
        catch(SQLException e) {
            System.out.println("Error inserting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void update(Notification notif) throws SQLException {
        String sql = "UPDATE notification SET id_package=?, id_user_target=?, message=?, "
                   + "type=?, is_read=?, date_notif=? WHERE id_notification=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notif.getIdPackage());
            stmt.setInt(2, notif.getIdUserTarget());
            stmt.setString(3, notif.getMessage());
            stmt.setString(4, notif.getType().name());
            stmt.setBoolean(5, notif.isRead());
            stmt.setTimestamp(6, notif.getDateNotif());
            stmt.setInt(7, notif.getIdNotification());

            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println("Error updating notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM notification WHERE id_notification=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
        catch(SQLException e) {
            System.out.println("Error deleting notification: " + e.getMessage());
            throw e;
        }
    }

    @Override
    public Notification findById(int id) throws SQLException {
        String sql = "SELECT * FROM notification WHERE id_notification=?";
        Notification notif = null;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    notif = mapNotification(rs);
                }
            }
        }
        catch(SQLException e) {
            System.out.println("Error finding notification: " + e.getMessage());
            throw e;
        }

        return notif;
    }

    @Override
    public List<Notification> findAll() throws SQLException {
        String sql = "SELECT * FROM notification";
        List<Notification> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapNotification(rs));
            }
        }
        catch(SQLException e) {
            System.out.println("Error retrieving notifications: " + e.getMessage());
            throw e;
        }

        return list;
    }

    private Notification mapNotification(ResultSet rs) throws SQLException {
        Notification n = new Notification();

        n.setIdNotification(rs.getInt("id_notification"));
        n.setIdPackage(rs.getInt("id_package"));
        n.setIdUserTarget(rs.getInt("id_user_target"));
        n.setMessage(rs.getString("message"));
        n.setType(NotificationType.valueOf(rs.getString("type")));
        n.setRead(rs.getBoolean("is_read"));
        n.setDateNotif(rs.getTimestamp("date_notif"));

        return n;
    }
}
