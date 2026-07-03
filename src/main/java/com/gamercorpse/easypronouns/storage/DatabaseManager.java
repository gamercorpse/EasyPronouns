package com.gamercorpse.easypronouns.storage;

import com.gamercorpse.easypronouns.EasyPronouns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public final class DatabaseManager {

    private final EasyPronouns plugin;
    private Connection connection;
    private boolean mysqlAvailable;

    public DatabaseManager(EasyPronouns plugin) {
        this.plugin = plugin;
    }

    public synchronized void connect() {
        mysqlAvailable = false;

        if (!isMysqlConfigured()) {
            return;
        }

        try {
            if (connection != null && !connection.isClosed()) {
                mysqlAvailable = true;
                return;
            }

            String host = plugin.getConfig().getString("storage.mysql.host", "localhost");
            int port = plugin.getConfig().getInt("storage.mysql.port", 3306);
            String database = plugin.getConfig().getString("storage.mysql.database", "easypronouns");
            String username = plugin.getConfig().getString("storage.mysql.username", "root");
            String password = plugin.getConfig().getString("storage.mysql.password", "password");
            boolean useSSL = plugin.getConfig().getBoolean("storage.mysql.useSSL", false);
            boolean allowPublicKeyRetrieval = plugin.getConfig().getBoolean("storage.mysql.allowPublicKeyRetrieval", true);

            String url = "jdbc:mysql://" + host + ":" + port + "/" + database
                    + "?useSSL=" + useSSL
                    + "&allowPublicKeyRetrieval=" + allowPublicKeyRetrieval
                    + "&autoReconnect=true"
                    + "&useUnicode=true"
                    + "&characterEncoding=utf8";

            connection = DriverManager.getConnection(url, username, password);
            mysqlAvailable = true;
        } catch (SQLException exception) {
            mysqlAvailable = false;
            plugin.getLogger().warning("Could not connect to MySQL: " + exception.getMessage());

            if (plugin.getConfig().getBoolean("storage.mysql.fallback-to-yaml", true)) {
                plugin.getLogger().warning("Falling back to YAML storage.");
            }
        }
    }

    public synchronized void createTables() {
        if (!mysqlAvailable) {
            return;
        }

        String table = getTable();

        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS `%s` (
                        uuid VARCHAR(36) NOT NULL PRIMARY KEY,
                        pronouns TEXT NOT NULL,
                        updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                    )
                    """.formatted(table));
        } catch (SQLException exception) {
            mysqlAvailable = false;
            plugin.getLogger().severe("Could not create database table: " + exception.getMessage());

            if (plugin.getConfig().getBoolean("storage.mysql.fallback-to-yaml", true)) {
                plugin.getLogger().warning("Falling back to YAML storage.");
            }
        }
    }

    public synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connect();
        }

        if (connection == null || !mysqlAvailable) {
            throw new SQLException("MySQL is not available.");
        }

        return connection;
    }

    public boolean isMysqlConfigured() {
        String type = plugin.getConfig().getString("storage.type", "mysql");
        boolean mysqlEnabled = plugin.getConfig().getBoolean("storage.mysql.enabled", true);
        return mysqlEnabled && type.equalsIgnoreCase("mysql");
    }

    public boolean isMysqlAvailable() {
        return mysqlAvailable;
    }

    public boolean shouldFallbackToYaml() {
        return plugin.getConfig().getBoolean("storage.mysql.fallback-to-yaml", true);
    }

    public String getTable() {
        return plugin.getConfig().getString("storage.mysql.table", "easy_pronouns");
    }

    public synchronized void close() {
        mysqlAvailable = false;

        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException exception) {
            plugin.getLogger().warning("Error closing database: " + exception.getMessage());
        }
    }
}