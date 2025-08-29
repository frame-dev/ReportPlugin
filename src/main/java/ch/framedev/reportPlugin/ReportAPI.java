package ch.framedev.reportPlugin;



/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 02.08.2025 20:26
 */

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class ReportAPI {

    private static ReportAPI instance;
    private final Database database = new Database(ReportPlugin.getInstance());

    private ReportAPI() {
        // Private constructor to prevent instantiation
    }

    public static ReportAPI getInstance() {
        if (instance == null) {
            instance = new ReportAPI();
        }
        return instance;
    }

    // Add methods to interact with the report system here
    // For example, methods to create, resolve, or list reports

    public void createReport(Player reportedPlayer, String reason, String reporter, String serverName, String serverAddress) {
        Report report = new Report(
                reportedPlayer.getName(),
                reason,
                reporter,
                java.util.UUID.randomUUID().toString(),
                serverName,
                serverAddress,
                "Bukkit Version",
                reportedPlayer.getWorld().getName(),
                Report.getLocationAsString(reportedPlayer.getLocation())
        );
        database.insertReport(report);
    }

    public void updateReport(Report report) {
        database.updateReport(report);
    }

    public boolean resolveReport(String reportedPlayer, String resolutionComment, boolean resolved) {
        Report report = database.getReportByPlayer(reportedPlayer);
        if (report != null) {
            report.setResolutionComment(resolutionComment);
            report.setResolved(true);
            database.updateReport(report);
            return true;
        } else {
            return false;
        }
    }

    public Report getReportById(String reportId) {
        return database.getReportById(reportId);
    }

    public boolean reportExists(String reportId) {
        return database.reportExists(reportId);
    }

    public boolean playerHasReport(String reportedPlayer) {
        return database.playerHasReport(reportedPlayer);
    }

    public void deleteReport(String reportId) {
        database.deleteReport(reportId);
    }

    public Report getReportByPlayer(String reportedPlayer) {
        return database.getReportByPlayer(reportedPlayer);
    }

    public Report getReportByReporter(String reporter) {
        return database.getReportByReporter(reporter);
    }

    public Database getDatabase() {
        return database;
    }
}
