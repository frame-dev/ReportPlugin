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
import ch.framedev.reportPlugin.utils.DiscordUtils;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Singleton class providing an API for managing player reports.
 * This class allows creating, updating, resolving, and retrieving reports,
 * as well as interacting with the underlying database and sending notifications via Discord webhooks.
 */
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

    public void createReport(Player reportedPlayer, String reason, String reporter, String serverName, String serverAddress) {
        Report report = new Report(
                reportedPlayer.getName(),
                reason,
                reporter,
                java.util.UUID.randomUUID().toString(),
                serverName,
                serverAddress,
                Bukkit.getVersion(),
                reportedPlayer.getWorld().getName(),
                Report.getLocationAsString(reportedPlayer.getLocation())
        );
        database.insertReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportToDiscord(ReportPlugin.getInstance(), report)) {
                ReportPlugin.getInstance().getLogger().warning("Failed to send report to Discord webhook for player: " + reportedPlayer.getName());
            }
        }
    }

    public void updateReport(Report report) {
        database.updateReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportUpdateToDiscord(report)) {
                ReportPlugin.getInstance().getLogger().warning("Failed to send report update to Discord webhook for report ID: " + report.getReportId());
            }
        }
    }

    public boolean resolveReport(String reportedPlayer, String resolutionComment, boolean resolved) {
        Report report = database.getReportByPlayer(reportedPlayer);
        if (report != null) {
            report.setResolutionComment(resolutionComment);
            report.setResolved(true);
            database.updateReport(report);
            if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
                if (!DiscordUtils.sendReportResolvedToDiscord(report)) {
                    ReportPlugin.getInstance().getLogger().warning("Failed to send resolved report to Discord webhook for player: " + reportedPlayer);
                }
            }
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

    public List<Report> getAllReports() {
        return database.getAllReports();
    }

    public int countReportsForPlayer(String reportedPlayer) {
        return database.countReportsForPlayer(reportedPlayer);
    }

    public List<Report> getAllReportsFromReportedPlayer(String reportedPlayer) {
        return database.getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer))
                .toList();
    }

    public List<Report> getAllReportsFromReporter(String reporter) {
        return database.getAllReports().stream()
                .filter(report -> report.getReporter().equalsIgnoreCase(reporter))
                .toList();
    }

    public List<Report> getAllUnresolvedReports() {
        return database.getAllReports().stream()
                .filter(report -> !report.isResolved())
                .toList();
    }

    public List<Report> getAllResolvedReports() {
        return database.getAllReports().stream()
                .filter(Report::isResolved)
                .toList();
    }

    public int countAllReports() {
        return database.getAllReports().size();
    }

    public int countAllUnresolvedReports() {
        return (int) database.getAllReports().stream()
                .filter(report -> !report.isResolved())
                .count();
    }

    public int countAllResolvedReports() {
        return (int) database.getAllReports().stream()
                .filter(Report::isResolved)
                .count();
    }

    public Map<String, Report> getUpdateHistory(String reportId) {
        return database.getUpdateHistory(database.getReportById(reportId));
    }

    public List<Report> getAllResolvedReportsByPlayer(String reportedPlayer) {
        return database.getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer) && report.isResolved())
                .toList();
    }

    public List<Report> getAllUnresolvedReportsByPlayer(String reportedPlayer) {
        return database.getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer) && !report.isResolved())
                .toList();
    }

    public boolean isResolved(String reportId) {
        Report report = database.getReportById(reportId);
        return report != null && report.isResolved();
    }

    public boolean isResolved(Report report) {
        return report != null && report.isResolved();
    }

    public void clearUpdateHistory(String reportId) {
        database.clearUpdateHistory(database.getReportById(reportId));
    }

    public void writeUpdateHistory(Report report, String updater) {
        database.writeUpdateHistory(report, updater);
    }

    public Map<String, Report> getUpdateHistory(Report report) {
        return database.getUpdateHistory(report);
    }
}
