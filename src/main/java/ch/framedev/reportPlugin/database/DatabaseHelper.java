package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 21:28
 */

import ch.framedev.reportPlugin.utils.Report;

import java.util.List;

public interface DatabaseHelper {

    /**
     * Insert a new report into the database
     * @param report the Report object to insert
     */
    void insertReport(Report report);
    /**
     * Retrieve a report by its ID
     * @param reportId the ID of the report
     * @return the Report object if found, null otherwise
     */
    Report getReportById(String reportId);
    /**
     * Retrieve a report by the reported player's name
     * @param reportedPlayer the name of the reported player
     * @return the Report object if found, null otherwise
     */
    Report getReportByReportedPlayer(String reportedPlayer);
    /**
     * Retrieve a report by the reporter's name
     * @param reporter the name of the reporter
     * @return the Report object if found, null otherwise
     */
    Report getReportByReporter(String reporter);
    /**
     * Retrieve all reports from the database
     * @return a list of all Report objects
     */
    List<Report> getAllReports();
    /**
     * Update an existing report in the database
     * @param report the Report object with updated information
     */
    void updateReport(Report report);
    /**
     * Delete a report by its ID
     * @param reportId the ID of the report to delete
     */
    boolean deleteReport(String reportId);
    /**
     * Create the reports table in the database if it does not exist
     */
    void createTable();
    /**
     * Retrieve a report by the reported player's name
     * @param reportedPlayer the name of the reported player
     * @return the Report object if found, null otherwise
     */
    Report getReportByPlayer(String reportedPlayer);
    /**
     * Check if a report exists by its ID
     * @param reportId the ID of the report
     * @return true if the report exists, false otherwise
     */
    boolean reportExists(String reportId);
    /**
     * Check if a player has any reports
     * @param reportedPlayer the name of the reported player
     * @return true if the player has reports, false otherwise
     */
    boolean playerHasReport(String reportedPlayer);
    /**
     * Count the number of reports for a specific player
     * @param reportedPlayer the name of the reported player
     * @return the number of reports for the player
     */
    int countReportsForPlayer(String reportedPlayer);

    /**
     * Establish a connection to the database
     * @return true if the connection was successful, false otherwise
     */
    boolean connect();
}
