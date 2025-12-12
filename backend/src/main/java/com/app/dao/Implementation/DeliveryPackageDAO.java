package com.app.dao.Implementation;

import com.app.dao.Interface.DAO;
import com.app.model.DeliveryPackage;
import com.app.model.Enums.PackageStatus;
import com.app.model.Enums.VehicleType;
import com.app.util.Database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeliveryPackageDAO implements DAO<DeliveryPackage> {

    @Override
    public void insert(DeliveryPackage pkg) throws SQLException {
        String sql = "INSERT INTO package (id_client_source, id_client_destination, vehicle_type_needed, " +
                     "address_source, address_destination, weight, price, dimensions, description, status, created_at) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, pkg.getIdClientSource());
            if (pkg.getIdClientDestination() > 0)
                stmt.setInt(2, pkg.getIdClientDestination());
            else
                stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, pkg.getVehicleTypeNeeded() != null ? pkg.getVehicleTypeNeeded().name() : null);
            stmt.setString(4, pkg.getAddressSource());
            stmt.setString(5, pkg.getAddressDestination());
            stmt.setFloat(6, pkg.getWeight());
            stmt.setFloat(7, pkg.getPrice());
            stmt.setString(8, pkg.getDimensions());
            stmt.setString(9, pkg.getDescription());
            stmt.setString(10, pkg.getStatus() != null ? pkg.getStatus().name() : PackageStatus.CREATED.name());
            stmt.setTimestamp(11, pkg.getCreatedAt() != null ? pkg.getCreatedAt() : new Timestamp(System.currentTimeMillis()));

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    pkg.setIdPackage(rs.getInt(1));
                }
            }
        }
    }

    @Override
    public void update(DeliveryPackage pkg) throws SQLException {
        String sql = "UPDATE package SET id_client_source=?, id_client_destination=?, vehicle_type_needed=?, " +
                     "address_source=?, address_destination=?, weight=?, price=?, dimensions=?, description=?, " +
                     "status=? WHERE id_package=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pkg.getIdClientSource());
            if (pkg.getIdClientDestination() > 0)
                stmt.setInt(2, pkg.getIdClientDestination());
            else
                stmt.setNull(2, Types.INTEGER);

            stmt.setString(3, pkg.getVehicleTypeNeeded() != null ? pkg.getVehicleTypeNeeded().name() : null);
            stmt.setString(4, pkg.getAddressSource());
            stmt.setString(5, pkg.getAddressDestination());
            stmt.setFloat(6, pkg.getWeight());
            stmt.setFloat(7, pkg.getPrice());
            stmt.setString(8, pkg.getDimensions());
            stmt.setString(9, pkg.getDescription());
            stmt.setString(10, pkg.getStatus() != null ? pkg.getStatus().name() : PackageStatus.CREATED.name());
            stmt.setInt(11, pkg.getIdPackage());

            stmt.executeUpdate();
        }
    }

    @Override
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM package WHERE id_package=?";

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    @Override
    public DeliveryPackage findById(int id) throws SQLException {
        String sql = "SELECT * FROM package WHERE id_package=?";
        DeliveryPackage pkg = null;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    pkg = mapRow(rs);
                }
            }
        }

        return pkg;
    }

    @Override
    public List<DeliveryPackage> findAll() throws SQLException {
        String sql = "SELECT * FROM package";
        List<DeliveryPackage> list = new ArrayList<>();

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }

        return list;
    }

    private DeliveryPackage mapRow(ResultSet rs) throws SQLException {
        DeliveryPackage pkg = new DeliveryPackage();

        pkg.setIdPackage(rs.getInt("id_package"));
        pkg.setIdClientSource(rs.getInt("id_client_source"));

        int dest = rs.getInt("id_client_destination");
        if (!rs.wasNull()) pkg.setIdClientDestination(dest);

        String vt = rs.getString("vehicle_type_needed");
        if (vt != null) pkg.setVehicleTypeNeeded(VehicleType.valueOf(vt));

        pkg.setAddressSource(rs.getString("address_source"));
        pkg.setAddressDestination(rs.getString("address_destination"));
        pkg.setWeight(rs.getFloat("weight"));
        pkg.setPrice(rs.getFloat("price"));
        pkg.setDimensions(rs.getString("dimensions"));
        pkg.setDescription(rs.getString("description"));

        String status = rs.getString("status");
        if (status != null) pkg.setStatus(PackageStatus.valueOf(status));

        pkg.setCreatedAt(rs.getTimestamp("created_at"));

        return pkg;
    }
}
