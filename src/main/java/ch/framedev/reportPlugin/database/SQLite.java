package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 21:26
 */

import java.sql.Connection;

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

    public Connection connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            return java.sql.DriverManager.getConnection(getDatabaseUrl());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
