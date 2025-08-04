package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin.database
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 04.08.2025 21:29
 */

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class MongoDBHelper implements DatabaseHelper {

    private final MongoDB mongoDb;

    public MongoDBHelper(ReportPlugin plugin) {
        String host = plugin.getConfig().getString("mongodb.host", "localhost");
        String database = plugin.getConfig().getString("mongodb.database", "reportPlugin");
        String username = plugin.getConfig().getString("mongodb.username", "root");
        String password = plugin.getConfig().getString("mongodb.password", "password");
        int port = plugin.getConfig().getInt("mongodb.port", 27017);
        this.mongoDb = new MongoDB(host, database, username, password, port);
        plugin.getLogger().info("Connecting to MongoDB at " + host + ":" + port + " with database " + database);
        this.mongoDb.connect();
        plugin.getLogger().info("MongoDB initialized successfully.");
    }

    private MongoCollection<Document> getReportsCollection() {
        return this.mongoDb.getDatabase().getCollection("reports");
    }

    @Override
    public void insertReport(Report report) {
        getReportsCollection().insertOne(report.toDocument());
    }

    @Override
    public Report getReportById(String reportId) {
        return getReportsCollection().find(new Document("reportId", reportId))
                .map(Report::new)
                .first();
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        return getReportsCollection().find(new Document("reportedPlayer", reportedPlayer))
                .map(Report::new)
                .first();
    }

    @Override
    public Report getReportByReporter(String reporter) {
        return getReportsCollection().find(new Document("reporter", reporter))
                .map(Report::new)
                .first();
    }

    @Override
    public List<Report> getAllReports() {
        return getReportsCollection().find()
                .map(Report::new)
                .into(new ArrayList<>());
    }

    @Override
    public void updateReport(Report report) {
        Document query = new Document("reportId", report.getReportId());
        Document update = new Document("$set", report.toDocument());
        getReportsCollection().updateOne(query, update);
    }

    @Override
    public void deleteReport(String reportId) {
        Document query = new Document("reportId", reportId);
        getReportsCollection().deleteOne(query);
    }

    @Override
    public void createTable() {
        // MongoDB does not require explicit table creation like SQL databases.
        // The collection will be created automatically when the first document is inserted.
    }

    @Override
    public Report getReportByPlayer(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer);
    }

    @Override
    public boolean reportExists(String reportId) {
        return getReportsCollection().find(new Document("reportId", reportId)).first() != null;
    }

    @Override
    public boolean playerHasReport(String reportedPlayer) {
        return getReportsCollection().find(new Document("reportedPlayer", reportedPlayer)).first() != null;
    }
}
