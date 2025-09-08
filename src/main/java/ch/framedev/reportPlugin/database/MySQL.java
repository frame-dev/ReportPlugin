package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:21
 */

import ch.framedev.reportPlugin.main.ReportPlugin;

import java.sql.Connection;
import java.util.logging.Level;

@SuppressWarnings("unused")
public class MySQL {

    private String host;
    private String database;
    private String username;
    private String password;
    private int port;

    public MySQL(String host, String database, String username, String password, int port) {
        this.host = host;
        this.database = database;
        this.username = username;
        this.password = password;
        this.port = port;
    }

    // Getters and setters for the fields
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Constructs the JDBC connection URL for MySQL.
     *
     * @return The JDBC connection URL as a String.
     */
    public String getConnectionUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
               "?useSSL=false&allowPublicKeyRetrieval=true";
    }

    /**
     * Establishes a connection to the MySQL database.
     *
     * @return A Connection object if successful, null otherwise.
     */
    public Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return java.sql.DriverManager.getConnection(getConnectionUrl(), username, password);
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "MySQL JDBC Driver not found. Please ensure the MySQL JDBC library is included in your project dependencies.", e);
        } catch (java.sql.SQLException e) {
            System.err.println("Failed to connect to MySQL database.");
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to connect to MySQL database at " + getConnectionUrl(), e);
        }
        return null;
    }
}
