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
 * as well as interacting with the underlying database and sending notifications
 * via Discord webhooks.
 */
public class ReportAPI {

    private static ReportAPI instance;

    private ReportAPI() {
        // Private constructor to prevent instantiation
    }

    /**
     * Returns the singleton instance of ReportAPI. If the instance does not exist,
     * it will be created.
     * 
     * @return the singleton instance of ReportAPI
     */
    public static ReportAPI getInstance() {
        if (instance == null) {
            instance = new ReportAPI();
        }
        return instance;
    }

    /**
     * Helper method to retrieve the Database instance from the ReportPlugin. This
     * method ensures that the plugin and database are available before returning
     * the database instance.
     * 
     * @return the Database instance from the ReportPlugin
     * @throws IllegalStateException if the ReportPlugin is not enabled or the
     *                               database is unavailable
     */
    private Database getDatabaseOrThrow() {
        ReportPlugin plugin = ReportPlugin.getInstance();
        if (plugin == null || plugin.getDatabase() == null) {
            throw new IllegalStateException("ReportPlugin is not enabled or the database is unavailable.");
        }

        return plugin.getDatabase();
    }

    /**
     * Creates a new report for a player.
     *
     * @param reportedPlayer the player being reported
     * @param reason         the reason for the report
     * @param reporter       the name of the player reporting
     * @param serverName     the name of the server where the report occurred
     * @param serverAddress  the address of the server where the report occurred
     */
    public void createReport(Player reportedPlayer, String reason, String reporter, String serverName,
            String serverAddress) {
        Report report = new Report(
                reportedPlayer.getName(),
                reason,
                reporter,
                java.util.UUID.randomUUID().toString(),
                serverName,
                serverAddress,
                Bukkit.getVersion(),
                reportedPlayer.getWorld().getName(),
                Report.getLocationAsString(reportedPlayer.getLocation()));
        getDatabaseOrThrow().insertReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportToDiscord(ReportPlugin.getInstance(), report)) {
                ReportPlugin.getInstance().getLogger()
                        .warning("Failed to send report to Discord webhook for player: " + reportedPlayer.getName());
            }
        }
    }

    /**
     * Updates an existing report.
     *
     * @param report the report to update
     */
    public void updateReport(Report report) {
        getDatabaseOrThrow().updateReport(report);
        if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!DiscordUtils.sendReportUpdateToDiscord(report)) {
                ReportPlugin.getInstance().getLogger().warning(
                        "Failed to send report update to Discord webhook for report ID: " + report.getReportId());
            }
        }
    }

    /**
     * Resolves a report for a player.
     *
     * @param reportedPlayer    the player being reported
     * @param resolutionComment the comment for the resolution
     * @param resolved          the status of the resolution
     * @return true if the report was successfully resolved, false otherwise
     */
    public boolean resolveReport(String reportedPlayer, String resolutionComment, boolean resolved) {
        Report report = getDatabaseOrThrow().getReportByPlayer(reportedPlayer);
        if (report != null) {
            report.setResolutionComment(resolutionComment);
            report.setStatus(resolved ? ReportStatus.RESOLVED : ReportStatus.OPEN);
            getDatabaseOrThrow().updateReport(report);
            if (ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
                if (resolved && !DiscordUtils.sendReportResolvedToDiscord(report)) {
                    ReportPlugin.getInstance().getLogger()
                            .warning("Failed to send resolved report to Discord webhook for player: " + reportedPlayer);
                }
            }
            return true;
        }

        return false;
    }

    /**
     * Retrieves a report by its ID.
     *
     * @param reportId the ID of the report
     * @return the report with the specified ID, or null if not found
     */
    public Report getReportById(String reportId) {
        return getDatabaseOrThrow().getReportById(reportId);
    }

    /**
     * Checks if a report exists by its ID.
     *
     * @param reportId the ID of the report
     * @return true if the report exists, false otherwise
     */
    public boolean reportExists(String reportId) {
        return getDatabaseOrThrow().reportExists(reportId);
    }

    /**
     * Checks if a player has any reports.
     *
     * @param reportedPlayer the name of the player
     * @return true if the player has reports, false otherwise
     */
    public boolean playerHasReport(String reportedPlayer) {
        return getDatabaseOrThrow().playerHasReport(reportedPlayer);
    }

    /**
     * Deletes a report by its ID.
     *
     * @param reportId the ID of the report to delete
     */
    public void deleteReport(String reportId) {
        getDatabaseOrThrow().deleteReport(reportId);
    }

    /**
     * Retrieves a report by the reported player's name.
     *
     * @param reportedPlayer the name of the reported player
     * @return the report for the specified player, or null if not found
     */
    public Report getReportByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getReportByPlayer(reportedPlayer);
    }

    /**
     * Retrieves a report by the reporter's name.
     *
     * @param reporter the name of the reporter
     * @return the report for the specified reporter, or null if not found
     */
    public Report getReportByReporter(String reporter) {
        return getDatabaseOrThrow().getReportByReporter(reporter);
    }

    /**
     * Retrieves the database instance.
     *
     * @return the database instance
     */
    public Database getDatabase() {
        return getDatabaseOrThrow();
    }

    /**
     * Retrieves all reports.
     *
     * @return a list of all reports
     */
    public List<Report> getAllReports() {
        return getDatabaseOrThrow().getAllReports();
    }

    /**
     * Counts the number of reports for a specific player.
     *
     * @param reportedPlayer the name of the reported player
     * @return the number of reports for the specified player
     */
    public int countReportsForPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().countReportsForPlayer(reportedPlayer);
    }

    /**
     * Retrieves all reports for a specific reported player.
     *
     * @param reportedPlayer the name of the reported player
     * @return a list of reports for the specified player
     */
    public List<Report> getAllReportsFromReportedPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer))
                .toList();
    }

    /**
     * Retrieves all reports from a specific reporter.
     *
     * @param reporter the name of the reporter
     * @return a list of reports from the specified reporter
     */
    public List<Report> getAllReportsFromReporter(String reporter) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReporter().equalsIgnoreCase(reporter))
                .toList();
    }

    /**
     * Retrieves all unresolved reports.
     *
     * @return a list of all unresolved reports
     */
    public List<Report> getAllUnresolvedReports() {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> !report.getStatus().isClosed())
                .toList();
    }

    /**
     * Retrieves all resolved reports.
     *
     * @return a list of all resolved reports
     */
    public List<Report> getAllResolvedReports() {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getStatus().isClosed())
                .toList();
    }

    /**
     * Counts all reports.
     *
     * @return the total number of reports
     */
    public int countAllReports() {
        return getDatabaseOrThrow().getAllReports().size();
    }

    /**
     * Counts all unresolved reports.
     *
     * @return the total number of unresolved reports
     */
    public int countAllUnresolvedReports() {
        return (int) getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> !report.getStatus().isClosed())
                .count();
    }

    /**
     * Counts all resolved reports.
     *
     * @return the total number of resolved reports
     */
    public int countAllResolvedReports() {
        return (int) getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getStatus().isClosed())
                .count();
    }

    /**
     * Retrieves the update history for a specific report by its ID.
     *
     * @param reportId the ID of the report
     * @return a map containing the update history of the report
     */
    public Map<String, Report> getUpdateHistory(String reportId) {
        Database database = getDatabaseOrThrow();
        return database.getUpdateHistory(database.getReportById(reportId));
    }

    /**
     * Retrieves all resolved reports for a specific reported player.
     *
     * @param reportedPlayer the name of the reported player
     * @return a list of resolved reports for the specified player
     */
    public List<Report> getAllResolvedReportsByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)
                        && report.getStatus().isClosed())
                .toList();
    }

    /**
     * Retrieves all unresolved reports for a specific reported player.
     *
     * @param reportedPlayer the name of the reported player
     * @return a list of unresolved reports for the specified player
     */
    public List<Report> getAllUnresolvedReportsByPlayer(String reportedPlayer) {
        return getDatabaseOrThrow().getAllReports().stream()
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)
                        && !report.getStatus().isClosed())
                .toList();
    }

    /**
     * Checks if a report is resolved by its ID.
     *
     * @param reportId the ID of the report
     * @return true if the report is resolved, false otherwise
     */
    public boolean isResolved(String reportId) {
        Report report = getDatabaseOrThrow().getReportById(reportId);
        return report != null && report.isResolved();
    }

    /**
     * Checks if a report is resolved.
     *
     * @param report the report to check
     * @return true if the report is resolved, false otherwise
     */
    public boolean isResolved(Report report) {
        return report != null && report.isResolved();
    }

    /**
     * Clears the update history for a specific report by its ID.
     *
     * @param reportId the ID of the report
     */
    public void clearUpdateHistory(String reportId) {
        Database database = getDatabaseOrThrow();
        database.clearUpdateHistory(database.getReportById(reportId));
    }

    /**
     * Writes an update to the history of a specific report.
     *
     * @param report  the report to update
     * @param updater the name of the person updating the report
     */
    public void writeUpdateHistory(Report report, String updater) {
        getDatabaseOrThrow().writeUpdateHistory(report, updater);
    }

    /**
     * Retrieves the update history for a specific report.
     *
     * @param report the report to retrieve the update history for
     * @return a map containing the update history of the report
     */
    public Map<String, Report> getUpdateHistory(Report report) {
        return getDatabaseOrThrow().getUpdateHistory(report);
    }
}
