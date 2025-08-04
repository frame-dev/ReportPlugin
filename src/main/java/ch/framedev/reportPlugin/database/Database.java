package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev.
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:20
 */

import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;

import java.util.List;

public class Database {

    private final DatabaseHelper databaseHelper;

    public Database(ReportPlugin plugin) {
        String databaseType = plugin.getConfig().getString("database", "mysql").toLowerCase();
        switch (databaseType) {
            case "mysql" -> this.databaseHelper = new MySQLHelper(plugin);
            case "sqlite" -> this.databaseHelper = new SQLiteHelper(plugin);
            case "mongodb" -> this.databaseHelper = new MongoDBHelper(plugin);
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        plugin.getLogger().info("Database initialized successfully using " + databaseType.toUpperCase() + ".");
    }

    public void createTable() {
        databaseHelper.createTable();
    }

    public void insertReport(Report report) {
        databaseHelper.insertReport(report);
    }

    public boolean reportExists(String reportId) {
        return databaseHelper.reportExists(reportId);
    }

    public boolean playerHasReport(String reportedPlayer) {
        return databaseHelper.playerHasReport(reportedPlayer);
    }

    public List<Report> getAllReports() {
        return databaseHelper.getAllReports();
    }

    public Report getReportByPlayer(String reportedPlayer) {
        return databaseHelper.getReportByPlayer(reportedPlayer);
    }

    public Report getReportByReporter(String reporter) {
        return databaseHelper.getReportByReporter(reporter);
    }

    public Report getReportById(String reportId) {
        return databaseHelper.getReportById(reportId);
    }

    public void updateReport(Report report) {
        databaseHelper.updateReport(report);
    }

    public void deleteReport(String reportId) {
        databaseHelper.deleteReport(reportId);
    }
}
