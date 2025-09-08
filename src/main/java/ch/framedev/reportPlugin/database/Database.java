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

    private final String databaseType;

    public Database(ReportPlugin plugin) {
        // Load the database type from the plugin configuration
        databaseType = plugin.getConfig().getString("database", "filesystem").toLowerCase();

        plugin.getLogger().info("Initializing database with type: " + databaseType);
        // Initialize the database helper based on the configured database type
        switch (databaseType) {
            case "mysql" -> this.databaseHelper = new MySQLHelper(plugin);
            case "sqlite" -> this.databaseHelper = new SQLiteHelper(plugin);
            case "mongodb" -> this.databaseHelper = new MongoDBHelper(plugin);
            case "filesystem" -> this.databaseHelper = new FileSystemHelper(plugin);
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        plugin.getLogger().info("Database initialized successfully using " + databaseType.toUpperCase() + ".");
    }

    /**
     * Returns the type of database being used.
     *
     * @return the database type as a string.
     */
    public String getDatabaseType() {
        return databaseType;
    }

    /**
     * Creates the necessary table in the database if it doesn't already exist.
     */
    @SuppressWarnings("unused")
    public void createTable() {
        databaseHelper.createTable();
    }

    /**
     * Inserts a new report into the database.
     *
     * @param report the Report object to be inserted.
     */
    public void insertReport(Report report) {
        databaseHelper.insertReport(report);
    }

    /**
     * Checks if a report with the specified ID exists in the database.
     *
     * @param reportId the unique identifier of the report.
     * @return true if the report exists, otherwise false.
     */
    public boolean reportExists(String reportId) {
        return databaseHelper.reportExists(reportId);
    }

    /**
     * Checks if there are any reports for a specific player.
     *
     * @param reportedPlayer the name of the player who was reported.
     * @return true if there are reports for the player, otherwise false.
     */
    public boolean playerHasReport(String reportedPlayer) {
        return databaseHelper.playerHasReport(reportedPlayer);
    }

    /**
     * Retrieves all reports from the database.
     *
     * @return a list of all Report objects.
     */
    public List<Report> getAllReports() {
        return databaseHelper.getAllReports();
    }

    /**
     * Retrieves a report from the database based on the reported player's name.
     *
     * @param reportedPlayer the name of the player who was reported.
     * @return the Report object if found, otherwise null.
     */
    public Report getReportByPlayer(String reportedPlayer) {
        return databaseHelper.getReportByPlayer(reportedPlayer);
    }

    /**
     * Retrieves a report from the database based on the reporter's name.
     *
     * @param reporter the name of the player who reported.
     * @return the Report object if found, otherwise null.
     */
    public Report getReportByReporter(String reporter) {
        return databaseHelper.getReportByReporter(reporter);
    }

    /**
     * Retrieves a report from the database based on its unique identifier.
     *
     * @param reportId the unique identifier of the report.
     * @return the Report object if found, otherwise null.
     */
    public Report getReportById(String reportId) {
        return databaseHelper.getReportById(reportId);
    }

    /**
     * Updates an existing report in the database.
     *
     * @param report the Report object containing updated information.
     */
    public void updateReport(Report report) {
        databaseHelper.updateReport(report);
    }

    /**
     * Deletes a report from the database based on its unique identifier.
     *
     * @param reportId the unique identifier of the report to be deleted.
     */
    public void deleteReport(String reportId) {
        databaseHelper.deleteReport(reportId);
    }

    /**
     * Counts the number of reports for a specific player.
     *
     * @param reportedPlayer the name of the reported player.
     * @return the count of reports for the specified player.
     */
    public int countReportsForPlayer(String reportedPlayer) {
        return databaseHelper.countReportsForPlayer(reportedPlayer);
    }

    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }
}
