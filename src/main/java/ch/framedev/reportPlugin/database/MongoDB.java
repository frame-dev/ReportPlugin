package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin.database
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 04.08.2025 21:30
 */

import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Collections;

@SuppressWarnings("unused")
public class MongoDB {

    private String host;
    private String databaseString;
    private String username;
    private String password;
    private int port;

    private MongoClient client;
    private MongoDatabase database;

    public MongoDB(String host, String databaseString, String username, String password, int port) {
        this.host = host;
        this.databaseString = databaseString;
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

    public String getDatabaseString() {
        return databaseString;
    }

    public void setDatabaseString(String databaseString) {
        this.databaseString = databaseString;
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

    public MongoDatabase getDatabase() {
        return database;
    }

    public MongoClient getClient() {
        return client;
    }

    /**
     * Connects to the MongoDB database using the provided credentials and settings.
     */
    public void connect() {
        MongoCredential credential = MongoCredential.createCredential(username, databaseString, password.toCharArray());
        this.client = MongoClients.create(
                MongoClientSettings.builder()
                        .credential(credential)
                        .applyToClusterSettings(builder ->
                                builder.hosts(Collections.singletonList(new ServerAddress(host, port))))
                        .build());
        this.database = this.client.getDatabase(databaseString);
    }
}
