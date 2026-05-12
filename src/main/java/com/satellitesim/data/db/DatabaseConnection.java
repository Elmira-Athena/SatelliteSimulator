package com.satellitesim.data.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Quản lý kết nối JDBC đến SQL Server.
 *
 * <p>Sử dụng pattern Singleton để đảm bảo chỉ có một nguồn kết nối
 * duy nhất trong toàn bộ ứng dụng.</p>
 *
 * <p><strong>Cấu hình kết nối:</strong> Mặc định kết nối đến localhost:1433.
 * Có thể thay đổi thông qua các setter hoặc cấu hình bên ngoài.</p>
 */
public class DatabaseConnection {

    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 1433;
    private static final String DEFAULT_DATABASE = "SatelliteSimulation";

    private static String host = DEFAULT_HOST;
    private static int port = DEFAULT_PORT;
    private static String database = DEFAULT_DATABASE;
    private static String username = "sa";
    private static String password = "HushStore@Secure2026!";

    /** Private constructor - Singleton pattern. */
    private DatabaseConnection() {
    }

    /**
     * Tạo và trả về một kết nối JDBC mới đến SQL Server.
     *
     * @return Connection object
     * @throws SQLException nếu không thể kết nối
     */
    public static Connection getConnection() throws SQLException {
        String url = String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=false;trustServerCertificate=true",
                host, port, database
        );
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * Kiểm tra kết nối đến database có hoạt động không.
     *
     * @return true nếu kết nối thành công
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }

    // ==================== Configuration Setters ====================

    public static void configure(String host, int port, String database, String username, String password) {
        DatabaseConnection.host = host;
        DatabaseConnection.port = port;
        DatabaseConnection.database = database;
        DatabaseConnection.username = username;
        DatabaseConnection.password = password;
    }

    public static void setHost(String host) {
        DatabaseConnection.host = host;
    }

    public static void setPort(int port) {
        DatabaseConnection.port = port;
    }

    public static void setDatabase(String database) {
        DatabaseConnection.database = database;
    }

    public static void setCredentials(String username, String password) {
        DatabaseConnection.username = username;
        DatabaseConnection.password = password;
    }
}
