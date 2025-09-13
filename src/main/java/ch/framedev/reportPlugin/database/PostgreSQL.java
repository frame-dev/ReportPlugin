package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;

import java.sql.Connection;
import java.util.logging.Level;

public class PostgreSQL {

    private final String host;
    private final int port;
    private final String databaseName;
    private final String username;
    private final String password;

    public PostgreSQL(String host, int port, String databaseName, String username, String password) {
        this.host = host;
        this.port = port;
        this.databaseName = databaseName;
        this.username = username;
        this.password = password;
    }

    public String getDatabaseUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    public Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            return java.sql.DriverManager.getConnection(getDatabaseUrl(), username, password);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "PostgreSQL JDBC driver not found. Please ensure the PostgreSQL JDBC library is included in your dependencies.", e);
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to connect to PostgreSQL database.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to PostgreSQL database: " + getDatabaseUrl(), e);
        }
        return null;
    }
}
