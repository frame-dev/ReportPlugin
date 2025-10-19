package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class YamlFileSystemHelper implements DatabaseHelper {

    private final ReportPlugin plugin;
    private final File reportsDir;

    public YamlFileSystemHelper(ReportPlugin plugin) {
        this.plugin = plugin;
        this.reportsDir = new File(plugin.getDataFolder(), "reports");
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create reports directory: " + reportsDir.getAbsolutePath());
        }
        plugin.getLogger().info("FileSystemHelper initialized. Reports directory: " + reportsDir.getAbsolutePath());
    }

    private File fileForReportId(String reportId) {
        return new File(reportsDir, "report_" + reportId + ".yml");
    }

    private void writeReportFile(File target, Report report) throws IOException {
        // Write to a temp file then move into place for atomic-ish replace
        File tmp = File.createTempFile("report_", ".yaml", reportsDir);
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(tmp);
        fileConfig.set("reportId", report.getReportId());
        fileConfig.set("reportedPlayer", report.getReportedPlayer());
        fileConfig.set("reporter", report.getReporter());
        fileConfig.set("reason", report.getReason());
        fileConfig.set("timestamp", report.getTimestamp());
        fileConfig.set("resolved", report.isResolved());
        fileConfig.set("additionalInfo", report.getAdditionalInfo());
        fileConfig.set("resolutionComment", report.getResolutionComment());
        fileConfig.set("location", report.getLocation());
        fileConfig.set("serverIp", report.getServerIp());
        fileConfig.set("world", report.getWorldName());
        fileConfig.set("serverName", report.getServerName());
        fileConfig.set("serverVersion", report.getServerVersion());
        fileConfig.save(tmp);
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if filesystem doesn't support atomic move
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Report readReportFile(File file) throws IOException {
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        String reportId = fileConfig.getString("reportId");
        String reportedPlayer = fileConfig.getString("reportedPlayer");
        String reporter = fileConfig.getString("reporter");
        String reason = fileConfig.getString("reason");
        long timestamp = fileConfig.getLong("timestamp");
        boolean resolved = fileConfig.getBoolean("resolved", false);
        String additionalInfo = fileConfig.getString("additionalInfo");
        String resolutionComment = fileConfig.getString("resolutionComment");
        String location = fileConfig.getString("location");
        String serverIp = fileConfig.getString("serverIp");
        String worldName = fileConfig.getString("world");
        String serverName = fileConfig.getString("serverName");
        String serverVersion = fileConfig.getString("serverVersion");

        Report report = new Report(reportedPlayer, reason, reporter, reportId, serverName, serverIp, serverVersion, worldName, location);
        report.setTimestamp(timestamp);
        report.setResolved(resolved);
        report.setAdditionalInfo(additionalInfo);
        report.setResolutionComment(resolutionComment);
        if( reportId == null || reportId.isEmpty()) {
            throw new IOException("Report file " + file.getName() + " is missing a valid reportId.");
        }
        return report;
    }

    @Override
    public void insertReport(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Cannot insert null or invalid report.");
            return;
        }
        String reportId = report.getReportId();
        File reportFile = fileForReportId(reportId);
        if (reportFile.exists()) {
            plugin.getLogger().log(Level.WARNING, "Report with ID " + reportId + " already exists. Use updateReport to modify it.");
            return;
        }
        try {
            writeReportFile(reportFile, report);
            plugin.getLogger().info("Inserted report with ID " + reportId);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write report file for ID " + reportId, e);
        }
    }

    @Override
    public Report getReportById(String reportId) {
        if(reportId == null || reportId.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Cannot get report with null or empty ID.");
            return null;
        }
        File reportFile = fileForReportId(reportId);
        if (!reportFile.exists()) {
            plugin.getLogger().log(Level.INFO, "No report found with ID " + reportId);
            return null;
        }
        try {
            return readReportFile(reportFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to read report file for ID " + reportId, e);
            return null;
        }
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        for(Report r : getAllReports()) {
            if(r.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public Report getReportByReporter(String reporter) {
        for(Report r : getAllReports()) {
            if(r.getReporter().equalsIgnoreCase(reporter)) {
                return r;
            }
        }
        return null;
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new java.util.ArrayList<>();
        File[] files = reportsDir.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    Report report = readReportFile(file);
                    reports.add(report);
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE, "Failed to read report file: " + file.getName(), e);
                }
            }
        }
        return reports;
    }

    @Override
    public void updateReport(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Cannot update null or invalid report.");
            return;
        }
        String reportId = report.getReportId();
        File reportFile = fileForReportId(reportId);
        if (!reportFile.exists()) {
            plugin.getLogger().log(Level.WARNING, "Report with ID " + reportId + " does not exist. Use insertReport to create it.");
            return;
        }
        try {
            writeReportFile(reportFile, report);
            plugin.getLogger().info("Updated report with ID " + reportId);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to update report file for ID " + reportId, e);
        }
    }

    @Override
    public boolean deleteReport(String reportId) {
        if(reportId == null || reportId.isEmpty()) {
            plugin.getLogger().log(Level.WARNING, "Cannot delete report with null or empty ID.");
            return false;
        }
        File reportFile = fileForReportId(reportId);
        if (!reportFile.exists()) {
            plugin.getLogger().log(Level.INFO, "No report found with ID " + reportId + " to delete.");
            return false;
        }
        if (reportFile.delete()) {
            plugin.getLogger().info("Deleted report with ID " + reportId);
            return true;
        } else {
            plugin.getLogger().log(Level.SEVERE, "Failed to delete report file for ID " + reportId);
            return false;
        }
    }

    @Override
    public void createTable() {
        // No-op for file system storage
    }

    @Override
    public Report getReportByPlayer(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer);
    }

    @Override
    public boolean reportExists(String reportId) {
        File reportFile = fileForReportId(reportId);
        return reportFile.exists();
    }

    @Override
    public boolean playerHasReport(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer) != null;
    }

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        int count = 0;
        for(Report r : getAllReports()) {
            if(r.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
                count++;
            }
        }
        return count;
    }

    @Override
    public boolean isResolved(String reportId) {
        Report report = getReportById(reportId);
        return report != null && report.isResolved();
    }

    @Override
    public boolean connect() {
        return true;
    }

    @Override
    public void disconnect() {
        // Nothing to do for filesystem
    }

    private static final String HISTORY_DIR_NAME = "history";
    private File getHistoryDir() {
        File historyDir = new File(reportsDir, HISTORY_DIR_NAME);
        if (!historyDir.exists() && !historyDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create history directory: " + historyDir.getAbsolutePath());
        }
        return historyDir;
    }

    private File historyFileForReportId(String reportId) {
        return new File(getHistoryDir(), "history_" + reportId + ".yml");
    }

    private Map<String, Report> readHistoryFile(File file) {
        if (!file.exists()) return new LinkedHashMap<>();
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(file);
        Map<String, Report> history = new LinkedHashMap<>();
        for (String key : fileConfig.getKeys(false)) {
            String reportId = fileConfig.getString(key + ".reportId");
            if (reportId == null) continue;
            String reportedPlayer = fileConfig.getString(key + ".reportedPlayer");
            String reporter = fileConfig.getString(key + ".reporter");
            String reason = fileConfig.getString(key + ".reason");
            long timestamp = fileConfig.getLong(key + ".timestamp");
            boolean resolved = fileConfig.getBoolean(key + ".resolved", false);
            String additionalInfo = fileConfig.getString(key + ".additionalInfo");
            String resolutionComment = fileConfig.getString(key + ".resolutionComment");
            String location = fileConfig.getString(key + ".location");
            String serverIp = fileConfig.getString(key + ".serverIp");
            String worldName = fileConfig.getString(key + ".world");
            String serverName = fileConfig.getString(key + ".serverName");
            String serverVersion = fileConfig.getString(key + ".serverVersion");
            Report report = new Report(reportedPlayer, reason, reporter, reportId, serverName, serverIp, serverVersion, worldName, location);
            report.setTimestamp(timestamp);
            report.setResolved(resolved);
            report.setAdditionalInfo(additionalInfo);
            report.setResolutionComment(resolutionComment);
            history.put(key, report);
        }
        return history;
    }

    private void writeHistoryFile(File target, Map<String, Report> history) throws IOException {
        // Write to a temp file then move into place for atomic-ish replace
        File tmp = File.createTempFile("history_", ".yml", getHistoryDir());
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(tmp);
        for (Map.Entry<String, Report> entry : history.entrySet()) {
            String key = entry.getKey();
            Report report = entry.getValue();
            fileConfig.set(key + ".reportId", report.getReportId());
            fileConfig.set(key + ".reportedPlayer", report.getReportedPlayer());
            fileConfig.set(key + ".reporter", report.getReporter());
            fileConfig.set(key + ".reason", report.getReason());
            fileConfig.set(key + ".timestamp", report.getTimestamp());
            fileConfig.set(key + ".resolved", report.isResolved());
            fileConfig.set(key + ".additionalInfo", report.getAdditionalInfo());
            fileConfig.set(key + ".resolutionComment", report.getResolutionComment());
            fileConfig.set(key + ".location", report.getLocation());
            fileConfig.set(key + ".serverIp", report.getServerIp());
            fileConfig.set(key + ".world", report.getWorldName());
            fileConfig.set(key + ".serverName", report.getServerName());
            fileConfig.set(key + ".serverVersion", report.getServerVersion());
        }
        fileConfig.save(tmp);
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if filesystem doesn't support atomic move
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    @Override
    public boolean writeToUpdateHistory(Report report, String updater) {
        if (report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "writeToUpdateHistory called with invalid report");
            return false;
        }
        File historyFile = historyFileForReportId(report.getReportId());
        try {
            Map<String, Report> history = readHistoryFile(historyFile);
            // Nutze Updater und Zeitstempel als eindeutigen Schlüssel
            String key = updater + "_-_" + System.currentTimeMillis();
            history.put(key, report);
            writeHistoryFile(historyFile, history);
            return true;
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not write update history for report " + report.getReportId(), e);
            return false;
        }
    }

    @Override
    public Map<String, Report> getUpdateHistory(Report report) {
        if (report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "getUpdateHistory called with invalid report");
            return Map.of();
        }
        File historyFile = historyFileForReportId(report.getReportId());
        try {
            return readHistoryFile(historyFile);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read update history for report " + report.getReportId(), e);
            return Map.of();
        }
    }

    @Override
    public boolean clearUpdateHistory(Report report) {
        if (report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "clearUpdateHistory called with invalid report");
            return false;
        }
        File historyFile = historyFileForReportId(report.getReportId());
        if (historyFile.exists() && !historyFile.delete()) {
            plugin.getLogger().log(Level.SEVERE, "Could not delete history file: " + historyFile.getAbsolutePath());
            return false;
        }
        return true;
    }
}
