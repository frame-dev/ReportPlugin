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
import ch.framedev.reportPlugin.utils.ReportStatus;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

/**
 * Singleton class providing an API for managing player reports.
 * This class allows creating, updating, resolving, and retrieving reports,
 * as well as interacting with the underlying database and sending notifications via Discord webhooks.
 */
public class ReportAPI {

    private static ReportAPI instance;

    private ReportAPI() {
        // Private constructor to prevent instantiation
    }

    public static ReportAPI getInstance() {
        if (instance == null) {
            instance = new ReportAPI();
        }
        return instance;
    }

    private Database getDatabaseOrThrow() {
        ReportPlugin plugin = ReportPlugin.getInstance();
        if (plugin == null || plugin.getDatabase() == null) {
            throw new IllegalStateException("ReportPlugin is not enabled or the database is unavailable.");
        }

        return plugin.getDatabase();
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
        getDatabaseOrThrow().insertReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportToDiscord(ReportPlugin.getInstance(), report)) {
                ReportPlugin.getInstance().getLogger().warning("Failed to send report to Discord webhook for player: " + reportedPlayer.getName());
            }
        }
    }

    public void updateReport(Report report) {
        getDatabaseOrThrow().updateReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportUpdateToDiscord(report)) {
                ReportPlugin.getInstance().getLogger().warning("Failed to send report update to Discord webhook for report ID: " + report.getReportId());
            }
        }
    }

    public boolean resolveReport(String reportedPlayer, String resolutionComment, boolean resolved) {
        Report report = getDatabaseOrThrow().getReportByPlayer(reportedPlayer);
        if (report != null) {
            report.setResolutionComment(resolutionComment);
            report.setStatus(resolved ? ReportStatus.RESOLVED : ReportStatus.OPEN);
            getDatabaseOrThrow().updateReport(report);
            if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
                if (resolved && !DiscordUtils.sendReportResolvedToDiscord(report)) {
                    ReportPlugin.getInstance().getLogger().warning("Failed to send resolved report to Discord webhook for player: " + reportedPlayer);
                }
            }
            return true;
        }

        return false;
    }

    public Report getReportById(String reportId) {
        return getDatabaseOrThrow().getReportById(reportId);
    }

    public boolean reportExists(String reportId) {
        return getDatabaseOrThrow().reportExists(reportId);
    }

    public boolean playerHasReport(String reportedPlayer) {
        return getDatabaseOrThrow().playerHasReport(reportedPlayer);
    }

    public void deleteReport(String reportId) {
        getDatabaseOrThrow().deleteReport(reportId);
    }

    public Report getReportByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getReportByPlayer(reportedPlayer);
    }

    public Report getReportByReporter(String reporter) {
        return getDatabaseOrThrow().getReportByReporter(reporter);
    }

    public Database getDatabase() {
        return getDatabaseOrThrow();
    }

    public List<Report> getAllReports() {
        return getDatabaseOrThrow().getAllReports();
    }

    public int countReportsForPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().countReportsForPlayer(reportedPlayer);
    }

    public List<Report> getAllReportsFromReportedPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer))
                .toList();
    }

    public List<Report> getAllReportsFromReporter(String reporter) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReporter().equalsIgnoreCase(reporter))
                .toList();
    }

    public List<Report> getAllUnresolvedReports() {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> !report.getStatus().isClosed())
                .toList();
    }

    public List<Report> getAllResolvedReports() {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getStatus().isClosed())
                .toList();
    }

    public int countAllReports() {
        return getDatabaseOrThrow().getAllReports().size();
    }

    public int countAllUnresolvedReports() {
        return (int) getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> !report.getStatus().isClosed())
                .count();
    }

    public int countAllResolvedReports() {
        return (int) getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getStatus().isClosed())
                .count();
    }

    public Map<String, Report> getUpdateHistory(String reportId) {
        Database database = getDatabaseOrThrow();
        return database.getUpdateHistory(database.getReportById(reportId));
    }

    public List<Report> getAllResolvedReportsByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer) && report.getStatus().isClosed())
                .toList();
    }

    public List<Report> getAllUnresolvedReportsByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer) && !report.getStatus().isClosed())
                .toList();
    }

    public boolean isResolved(String reportId) {
        Report report = getDatabaseOrThrow().getReportById(reportId);
        return report != null && report.isResolved();
    }

    public boolean isResolved(Report report) {
        return report != null && report.isResolved();
    }

    public void clearUpdateHistory(String reportId) {
        Database database = getDatabaseOrThrow();
        database.clearUpdateHistory(database.getReportById(reportId));
    }

    public void writeUpdateHistory(Report report, String updater) {
        getDatabaseOrThrow().writeUpdateHistory(report, updater);
    }

    public Map<String, Report> getUpdateHistory(Report report) {
        return getDatabaseOrThrow().getUpdateHistory(report);
    }
}
