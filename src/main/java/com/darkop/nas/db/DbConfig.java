package com.darkop.nas.db;

import com.darkop.nas.config.NasConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConfig {

    static {
        try {
            // Explicit driver load (good practice for clarity)
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("PostgreSQL JDBC Driver not found", e);
        }
    }

    // Prevent instantiation
    private DbConfig() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(NasConfig.dbUrl(), NasConfig.dbUser(), NasConfig.dbPassword());
    }
}
