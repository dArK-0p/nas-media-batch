package com.darkop.nas.config;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public final class NasConfig {

    private static final Properties PROPS = new Properties();

    static {
        String externalPath = System.getProperty("nas.config");

        try (InputStream is = externalPath != null ? new FileInputStream(externalPath) : NasConfig.class.getClassLoader().getResourceAsStream("application.properties")) {

            if (is == null) {
                throw new RuntimeException("application.properties not found on classpath");
            }

            PROPS.load(is);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load application.properties", e);
        }
    }

    private NasConfig() {
        // no instances
    }

    public static String dbUrl() {
        return require("db.url");
    }

    public static String dbUser() {
        return require("db.username");
    }

    public static String dbPassword() {
        return require("db.password");
    }

    public static String uploadsRoot() {
        return require("uploads.root");
    }

    public static String mediaRoot() {
        return require("media.root");
    }

    public static String quarantineRoot() {
        return require("quarantine.root");
    }


    private static String require(String key) {
        String value = PROPS.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new RuntimeException("Missing required config: " + key);
        }
        return value;
    }
}
