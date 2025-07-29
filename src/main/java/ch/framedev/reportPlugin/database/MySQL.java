package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:21
 */

import java.sql.Connection;

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

    public String getConnectionUrl() {
        return "jdbc:mysql://" + host + ":" + port + "/" + database +
               "?useSSL=false&allowPublicKeyRetrieval=true" +
               "&user=" + username +
               "&password=" + password;
    }

    public Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return java.sql.DriverManager.getConnection(getConnectionUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
