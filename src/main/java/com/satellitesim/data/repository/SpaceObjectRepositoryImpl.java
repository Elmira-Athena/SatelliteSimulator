package com.satellitesim.data.repository;

import com.satellitesim.core.models.GroundStation;
import com.satellitesim.core.models.Satellite;
import com.satellitesim.core.models.SpaceObject;
import com.satellitesim.core.models.SpaceObjectType;
import com.satellitesim.data.db.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementation JDBC của SpaceObjectRepository.
 *
 * <p>Thao tác CRUD trên bảng SpaceObjects trong SQL Server.</p>
 *
 * <p><strong>Schema bảng SpaceObjects:</strong></p>
 * <pre>
 * | Column    | Type         | Description                    |
 * |-----------|--------------|--------------------------------|
 * | ID        | NVARCHAR(50) | Primary Key (UUID)             |
 * | Name      | NVARCHAR(100)| Tên vật thể                    |
 * | Type      | INT          | 0 = GroundStation, 1 = Satellite|
 * | Latitude  | FLOAT        | Vĩ độ (degrees)                |
 * | Longitude | FLOAT        | Kinh độ (degrees)              |
 * | Altitude  | FLOAT        | Độ cao (km)                    |
 * </pre>
 */
public class SpaceObjectRepositoryImpl implements SpaceObjectRepository {

    // ==================== SQL Statements ====================

    private static final String SQL_FIND_ALL =
            "SELECT ID, Name, Type, Latitude, Longitude, Altitude FROM SpaceObjects";

    private static final String SQL_FIND_BY_ID =
            "SELECT ID, Name, Type, Latitude, Longitude, Altitude FROM SpaceObjects WHERE ID = ?";

    private static final String SQL_INSERT =
            "INSERT INTO SpaceObjects (ID, Name, Type, Latitude, Longitude, Altitude) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
            "UPDATE SpaceObjects SET Name = ?, Type = ?, Latitude = ?, Longitude = ?, Altitude = ? WHERE ID = ?";

    private static final String SQL_DELETE =
            "DELETE FROM SpaceObjects WHERE ID = ?";

    private static final String SQL_EXISTS =
            "SELECT COUNT(1) FROM SpaceObjects WHERE ID = ?";

    // ==================== CRUD Operations ====================

    @Override
    public List<SpaceObject> findAll() {
        List<SpaceObject> result = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(SQL_FIND_ALL)) {

            while (rs.next()) {
                SpaceObject obj = mapResultSetToSpaceObject(rs);
                if (obj != null) {
                    result.add(obj);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding all SpaceObjects: " + e.getMessage());
        }
        return result;
    }

    @Override
    public Optional<SpaceObject> findById(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_FIND_BY_ID)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(mapResultSetToSpaceObject(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error finding SpaceObject by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean save(SpaceObject spaceObject) {
        if (exists(spaceObject.getId())) {
            return update(spaceObject);
        } else {
            return insert(spaceObject);
        }
    }

    @Override
    public boolean deleteById(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_DELETE)) {

            pstmt.setString(1, id);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting SpaceObject: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int saveAll(List<SpaceObject> spaceObjects) {
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {
                for (SpaceObject obj : spaceObjects) {
                    setInsertParameters(pstmt, obj);
                    pstmt.addBatch();
                }
                int[] results = pstmt.executeBatch();
                conn.commit();
                for (int r : results) {
                    if (r > 0) count++;
                }
            } catch (SQLException e) {
                conn.rollback();
                System.err.println("Error in batch save, rolled back: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Error getting connection for batch save: " + e.getMessage());
        }
        return count;
    }

    // ==================== Helper Methods ====================

    private boolean exists(String id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_EXISTS)) {

            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error checking existence: " + e.getMessage());
        }
        return false;
    }

    private boolean insert(SpaceObject obj) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_INSERT)) {

            setInsertParameters(pstmt, obj);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error inserting SpaceObject: " + e.getMessage());
            return false;
        }
    }

    private boolean update(SpaceObject obj) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(SQL_UPDATE)) {

            pstmt.setString(1, obj.getName());
            pstmt.setInt(2, obj.getType().getCode());
            pstmt.setDouble(3, obj.getLatitude());
            pstmt.setDouble(4, obj.getLongitude());
            pstmt.setDouble(5, obj.getAltitude());
            pstmt.setString(6, obj.getId());
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating SpaceObject: " + e.getMessage());
            return false;
        }
    }

    private void setInsertParameters(PreparedStatement pstmt, SpaceObject obj) throws SQLException {
        pstmt.setString(1, obj.getId());
        pstmt.setString(2, obj.getName());
        pstmt.setInt(3, obj.getType().getCode());
        pstmt.setDouble(4, obj.getLatitude());
        pstmt.setDouble(5, obj.getLongitude());
        pstmt.setDouble(6, obj.getAltitude());
    }

    /**
     * Map một dòng ResultSet sang SpaceObject (Satellite hoặc GroundStation)
     * dựa vào cột Type.
     */
    private SpaceObject mapResultSetToSpaceObject(ResultSet rs) throws SQLException {
        String id = rs.getString("ID");
        String name = rs.getString("Name");
        int typeCode = rs.getInt("Type");
        double latitude = rs.getDouble("Latitude");
        double longitude = rs.getDouble("Longitude");
        double altitude = rs.getDouble("Altitude");

        SpaceObjectType type = SpaceObjectType.fromCode(typeCode);

        return switch (type) {
            case SATELLITE -> new Satellite(id, name, latitude, longitude, altitude);
            case GROUND_STATION -> new GroundStation(id, name, latitude, longitude, altitude);
        };
    }
}
