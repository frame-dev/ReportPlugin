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
import java.util.Map;

public class Database {

    // The database helper instance that provides database-specific operations
    private final DatabaseHelper databaseHelper;

    // The type of database being used (e.g., "mysql", "sqlite", "mongodb", "postgresql", "h2, "jsonfilesystem")
    private final String databaseType;

    /**
     * Constructs a Database instance based on the configuration provided by the ReportPlugin.
     *
     * @param plugin the ReportPlugin instance containing the configuration.
     */
    public Database(ReportPlugin plugin) {
        // Load the database type from the plugin configuration
        databaseType = plugin.getConfig().getString("database", "jsonfilesystem").toLowerCase();

        plugin.getLogger().info("Initializing database with type: " + databaseType);

        // Initialize the database helper based on the configured database type
        switch (databaseType) {
            case "mysql" -> this.databaseHelper = new MySQLHelper(plugin);
            case "sqlite" -> this.databaseHelper = new SQLiteHelper(plugin);
            case "postgresql" -> this.databaseHelper = new PostgreSQLHelper(plugin);
            case "h2" -> this.databaseHelper = new H2StorageHelper(plugin);
            case "mongodb" -> this.databaseHelper = new MongoDBHelper(plugin);
            case "jsonfilesystem" -> this.databaseHelper = new JsonFileSystemHelper(plugin);
            case "yamlfilesystem" -> this.databaseHelper = new YamlFileSystemHelper(plugin);
            default -> throw new IllegalArgumentException("Unsupported database type: " + databaseType);
        }
        plugin.getLogger().info("Database initialized successfully using " + databaseType.toUpperCase() + ".");
    }

    /**
     * Connects to the database using the underlying DatabaseHelper.
     *
     * @return true if the connection was successful, otherwise false.
     */
    public boolean connect() {
        return databaseHelper.connect();
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
     * @return true if the report was successfully deleted, otherwise false.
     */
    public boolean deleteReport(String reportId) {
        return databaseHelper.deleteReport(reportId);
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

    /**
     * Returns the underlying DatabaseHelper instance.
     *
     * @return the DatabaseHelper instance.
     */
    public DatabaseHelper getDatabaseHelper() {
        return databaseHelper;
    }

    public boolean writeUpdateHistory(Report report, String updater) {
        return databaseHelper.writeToUpdateHistory(report, updater);
    }

    public Map<String, Report> getUpdateHistory(Report report) {
        return databaseHelper.getUpdateHistory(report);
    }

    public boolean clearUpdateHistory(Report report) {
        return databaseHelper.clearUpdateHistory(report);
    }
}
