package ch.framedev.reportPlugin.database;

/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 21:26
 */

import ch.framedev.reportPlugin.main.ReportPlugin;

import java.sql.Connection;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class H2Storage {

    private String path;
    private String databaseName;

    public H2Storage(String path, String databaseName) {
        this.path = path;
        this.databaseName = databaseName;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseUrl() {
        return "jdbc:h2:" + path + "/" + databaseName;
    }

    /**
     * Establishes and returns a connection to the H2 database.
     *
     * @return A Connection object if the connection is successful, null otherwise.
     */
    public Connection connect() {
        try {
            Class.forName("org.h2.Driver");
            return java.sql.DriverManager.getConnection(getDatabaseUrl(), "sa", "");
        } catch (ClassNotFoundException e) {
            System.err.println("H2 JDBC driver not found.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "H2 JDBC driver not found. Please ensure the H2 JDBC library is included in your project dependencies.", e);
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to connect to H2 database.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to H2 database at " + getDatabaseUrl(), e);
        }
        return null;
    }

    public void disconnect() {
        // H2 database connections are typically closed after each operation.
        // If you maintain a persistent connection, implement the logic to close it here.
    }
}
