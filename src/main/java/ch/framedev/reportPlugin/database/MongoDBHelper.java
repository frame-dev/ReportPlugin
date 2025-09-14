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
import java.util.Map;

public class MongoDBHelper implements DatabaseHelper {

    private final MongoDB mongoDb;

    /**
     * Initializes the MongoDBHelper with the specified ReportPlugin instance.
     * Connects to the MongoDB database using configuration from the plugin.
     *
     * @param plugin The ReportPlugin instance for logging and configuration access.
     */
    public MongoDBHelper(ReportPlugin plugin) {
        String host = plugin.getConfig().getString("mongodb.host", "localhost");
        String database = plugin.getConfig().getString("mongodb.database", "reportPlugin");
        String username = plugin.getConfig().getString("mongodb.username", "root");
        String password = plugin.getConfig().getString("mongodb.password", "password");
        int port = plugin.getConfig().getInt("mongodb.port", 27017);
        this.mongoDb = new MongoDB(host, database, username, password, port);
        plugin.getLogger().info("Connecting to MongoDB at " + host + ":" + port + " with database " + database);
        this.mongoDb.connect();
        createTable();
        plugin.getLogger().info("MongoDB initialized successfully.");
    }

    private MongoCollection<Document> getReportsCollection() {
        return this.mongoDb.getDatabase().getCollection("reports");
    }

    private MongoCollection<Document> getUpdateHistoryCollection() {
        return this.mongoDb.getDatabase().getCollection("update_history");
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
    public boolean deleteReport(String reportId) {
        Document query = new Document("reportId", reportId);
        long count = getReportsCollection().deleteOne(query).getDeletedCount();
        return count > 0;
    }

    @Override
    public void createTable() {
        // MongoDB does not require explicit table creation like SQL databases.
        // The collection will be created automatically when the first document is inserted.
        ReportPlugin.getInstance().getLogger().info("MongoDB does not require explicit table creation. Collection will be created on first insert.");
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

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        return (int) getReportsCollection().countDocuments(new Document("reportedPlayer", reportedPlayer));
    }

    @Override
    public boolean connect() {
        return mongoDb.getDatabase() != null;
    }

    @Override
    public boolean writeToUpdateHistory(Report report, String updater) {
        Document historyEntry = new Document()
                .append("reportId", report.getReportId())
                .append("updater", updater)
                .append("timestamp", System.currentTimeMillis())
                .append("reportData", report.toDocument());
        getUpdateHistoryCollection().insertOne(historyEntry);
        return true;
    }

    @Override
    public Map<String, Report> getUpdateHistory(Report report) {
        Map<String, Report> history = new java.util.HashMap<>();
        getUpdateHistoryCollection().find(new Document("reportId", report.getReportId()))
                .forEach(doc -> history.put(
                        doc.getString("updater") + " at " + doc.getLong("timestamp"),
                        new Report((Document) doc.get("reportData"))
                ));
        return history;
    }

    @Override
    public boolean clearUpdateHistory(Report report) {
        Document query = new Document("reportId", report.getReportId());
        long count = getUpdateHistoryCollection().deleteMany(query).getDeletedCount();
        return count > 0;
    }

    @Override
    public boolean isResolved(String reportId) {
        Report report = getReportById(reportId);
        return report != null && report.isResolved();
    }
}
