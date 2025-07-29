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

    void insertReport(Report report);
    Report getReportById(String reportId);
    Report getReportByReportedPlayer(String reportedPlayer);
    Report getReportByReporter(String reporter);
    List<Report> getAllReports();
    void updateReport(Report report);
    void deleteReport(String reportId);
    void createTable();
    Report getReportByPlayer(String reportedPlayer);
    boolean reportExists(String reportId);
    boolean playerHasReport(String reportedPlayer);
}
