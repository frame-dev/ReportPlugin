package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;

public record PostgreSQL(String host, int port, String databaseName, String username, String password) {

    public String getDatabaseUrl() {
        return "jdbc:postgresql://" + host + ":" + port + "/" + databaseName;
    }

    public Connection connect() {
        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(getDatabaseUrl(), username, password);
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC driver not found.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "PostgreSQL JDBC driver not found. Please ensure the PostgreSQL JDBC library is included in your dependencies.", e);
        } catch (SQLException e) {
            System.err.println("Failed to connect to PostgreSQL database.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to PostgreSQL database: " + getDatabaseUrl(), e);
        }
        return null;
    }
}
