package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ConnectorBase {
    private static final String URL = "jdbc:postgresql://localhost:5432/tu_base_de_datos";
    private static final String USER = "tu_usuario";
    private static final String PASSWORD = "tu_contraseña";

    public static String getDBUrl() {
        return URL;
    }

    public static String getDBUser() {
        return USER;
    }

    public static String getDBPassword() {
        return PASSWORD;
    }

    public static void insertRecord(String domain, String ipAddress) throws SQLException {
        try (Connection connection = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String sql = "INSERT INTO master_file (domain, ip_address) VALUES (?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, domain);
                statement.setString(2, ipAddress);
                statement.executeUpdate();
            }
        }
    }
}

