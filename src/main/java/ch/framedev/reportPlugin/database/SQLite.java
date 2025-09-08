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
public class SQLite {

    private String path;
    private String databaseName;

    public SQLite(String path, String databaseName) {
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
        return "jdbc:sqlite:" + path + "/" + databaseName;
    }

    /**
     * Establishes and returns a connection to the SQLite database.
     *
     * @return A Connection object if the connection is successful, null otherwise.
     */
    public Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            return java.sql.DriverManager.getConnection(getDatabaseUrl());
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC driver not found.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "SQLite JDBC driver not found. Please ensure the SQLite JDBC library is included in your project dependencies.", e);
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to connect to SQLite database.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to SQLite database at " + getDatabaseUrl(), e);
        }
        return null;
    }
}
