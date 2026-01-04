package com.darkop.nas.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConfig {

    // Prevent instantiation
    private DbConfig() {}

    // TODO: Externalize later (env vars / config file)
    private static final String JDBC_URL =
            "jdbc:postgresql://localhost:5432/nas_db";

    private static final String DB_USER = "nas_admin";
    private static final String DB_PASSWORD = "mcpojohn117";

    static {
        try {
            // Explicit driver load (good practice for clarity)
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(
                    "PostgreSQL JDBC Driver not found", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                JDBC_URL,
                DB_USER,
                DB_PASSWORD
        );
    }
}
