package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class TextFileSystemHelper implements DatabaseHelper {

    private final File reportsDir;
    private final ReportPlugin plugin;

    public TextFileSystemHelper(ReportPlugin plugin) {
        this.plugin = plugin;
        this.reportsDir = new File(plugin.getDataFolder(), "reports");
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create reports directory at " + reportsDir.getAbsolutePath());
        }
        plugin.getLogger().info("TextFileSystemHelper initialized. Reports directory: " + reportsDir.getAbsolutePath());
    }

    private void saveReportToFile(File target, Report report) {
        StringBuilder data = new StringBuilder();
        data.append("reporter").append(report.getReporter()).append(";")
                .append("reportId").append(report.getReportId()).append(";")
                .append("reportedPlayer").append(report.getReportedPlayer()).append(";")
                .append("reason").append(report.getReason()).append(";")
                .append("serverName").append(report.getServerName()).append(";")
                .append("serverIp").append(report.getServerIp()).append(";")
                .append("serverVersion").append(report.getServerVersion()).append(";")
                .append("location").append(report.getLocation()).append(";")
                .append("worldName").append(report.getWorldName()).append(";")
                .append("isResolved").append(report.isResolved()).append(";")
                .append("timestamp").append(report.getTimestamp()).append(";")
                .append("resolutionComment").append(report.getResolutionComment()).append(";")
                .append("additionalInfo").append(report.getAdditionalInfo()).append(";");
        // Write 'data' to the file at 'filePath'
        try (FileWriter writer = new FileWriter(target, false)) {
            writer.write(data.toString());
        } catch (Exception e) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to save report to file: " + e.getMessage(), e);
        }
    }

    // java
    private Report readReportFromFile(File file) {
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            String[] parts = content.toString().split(";");
            Report report = new Report();

            for (String part : parts) {
                if (part == null || part.isEmpty()) continue;

                if (part.startsWith("reporter")) {
                    report.setReporter(part.substring("reporter".length()));
                } else if (part.startsWith("reportId")) {
                    report.setReportId(part.substring("reportId".length()));
                } else if (part.startsWith("reportedPlayer")) {
                    report.setReportedPlayer(part.substring("reportedPlayer".length()));
                } else if (part.startsWith("reason")) {
                    report.setReason(part.substring("reason".length()));
                } else if (part.startsWith("serverName")) {
                    report.setServerName(part.substring("serverName".length()));
                } else if (part.startsWith("serverIp")) {
                    report.setServerIp(part.substring("serverIp".length()));
                } else if (part.startsWith("serverVersion")) {
                    report.setServerVersion(part.substring("serverVersion".length()));
                } else if (part.startsWith("location")) {
                    report.setLocation(part.substring("location".length()));
                } else if (part.startsWith("worldName")) {
                    report.setWorldName(part.substring("worldName".length()));
                } else if (part.startsWith("isResolved")) {
                    report.setResolved(Boolean.parseBoolean(part.substring("isResolved".length())));
                } else if (part.startsWith("timestamp")) {
                    report.setTimestamp(Long.parseLong(part.substring("timestamp".length())));
                } else if (part.startsWith("resolutionComment")) {
                    report.setResolutionComment(part.substring("resolutionComment".length()));
                } else if (part.startsWith("additionalInfo")) {
                    report.setAdditionalInfo(part.substring("additionalInfo".length()));
                }
            }
            return report;
        } catch (Exception e) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to read report from file: " + e.getMessage(), e);
        }
        return null;
    }


    @Override
    public void insertReport(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            ReportPlugin.getInstance().getLogger().log(Level.WARNING, "Cannot insert null report or report with empty ID.");
            return;
        }
        String reportId = report.getReportId();
        File reportFile = new File(reportsDir, reportId + ".txt");
        saveReportToFile(reportFile, report);
    }

    @Override
    public Report getReportById(String reportId) {
        if(reportId == null || reportId.isEmpty()) {
            plugin.getLogger().warning("Cannot get report with null or empty ID.");
            return null;
        }
        File reportFile = new File(reportsDir, reportId + ".txt");
        if(!reportFile.exists()) {
            plugin.getLogger().warning("Report file not found for ID: " + reportId);
            return null;
        }
        return readReportFromFile(reportFile);
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        for(Report report : getAllReports()) {
            if(report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public Report getReportByReporter(String reporter) {
        for(Report report : getAllReports()) {
            if(report.getReporter().equalsIgnoreCase(reporter)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new java.util.ArrayList<>();
        File[] files = reportsDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files != null) {
            for (File file : files) {
                Report report = readReportFromFile(file);
                if (report != null) {
                    reports.add(report);
                }
            }
        }
        return reports;
    }

    @Override
    public void updateReport(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            ReportPlugin.getInstance().getLogger().log(Level.WARNING, "Cannot update null report or report with empty ID.");
            return;
        }
        String reportId = report.getReportId();
        File reportFile = new File(reportsDir, reportId + ".txt");
        saveReportToFile(reportFile, report);
    }

    @Override
    public boolean deleteReport(String reportId) {
        if(reportId == null || reportId.isEmpty()) {
            plugin.getLogger().warning("Cannot delete report with null or empty ID.");
            return false;
        }
        File reportFile = new File(reportsDir, reportId + ".txt");
        if(reportFile.exists()) {
            return reportFile.delete();
        } else {
            plugin.getLogger().warning("Report file not found for ID: " + reportId);
            return false;
        }
    }

    @Override
    public void createTable() {
        // No action needed for text file system
    }

    @Override
    public Report getReportByPlayer(String reportedPlayer) {
        for(Report report : getAllReports()) {
            if(report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public boolean reportExists(String reportId) {
        File reportFile = new File(reportsDir, reportId + ".txt");
        return reportFile.exists();
    }

    @Override
    public boolean playerHasReport(String reportedPlayer) {
        for(Report report : getAllReports()) {
            if(report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        int count = 0;
        for(Report report : getAllReports()) {
            if(report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)) {
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
        // No action needed for text file system
    }

    private void writeUpdateHistory(File target, Map<String, Report> history) {
        StringBuilder data = new StringBuilder();
        for (Map.Entry<String, Report> entry : history.entrySet()) {
            data.append(entry.getKey()).append("=>").append(entry.getValue().getReportId()).append(";;");
        }
        try (FileWriter writer = new FileWriter(target, false)) {
            writer.write(data.toString());
        } catch (Exception e) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to save update history to file: " + e.getMessage(), e);
        }
    }

    private Map<String, Report> readUpdateHistory(File file) {
        Map<String, Report> history = new java.util.HashMap<>();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            StringBuilder content = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line);
            }

            String[] entries = content.toString().split(";;");
            for (String entry : entries) {
                if (entry == null || entry.isEmpty()) continue;

                String[] keyValue = entry.split("=>");
                if (keyValue.length == 2) {
                    String key = keyValue[0];
                    String reportId = keyValue[1];
                    Report report = getReportById(reportId);
                    if (report != null) {
                        history.put(key, report);
                    }
                }
            }
        } catch (Exception e) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Failed to read update history from file: " + e.getMessage(), e);
        }
        return history;
    }

    @Override
    public boolean writeToUpdateHistory(Report report, String updater) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            ReportPlugin.getInstance().getLogger().log(Level.WARNING, "Cannot write to update history for null report or report with empty ID.");
            return false;
        }
        String reportId = report.getReportId();
        File historyFile = new File(reportsDir, reportId + "_history.txt");
        Map<String, Report> history = readUpdateHistory(historyFile);
        history.put(updater, report);
        writeUpdateHistory(historyFile, history);
        return true;
    }

    @Override
    public Map<String, Report> getUpdateHistory(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            ReportPlugin.getInstance().getLogger().log(Level.WARNING, "Cannot get update history for null report or report with empty ID.");
            return new java.util.HashMap<>();
        }
        String reportId = report.getReportId();
        File historyFile = new File(reportsDir, reportId + "_history.txt");
        return readUpdateHistory(historyFile);
    }

    @Override
    public boolean clearUpdateHistory(Report report) {
        if(report == null || report.getReportId() == null || report.getReportId().isEmpty()) {
            ReportPlugin.getInstance().getLogger().log(Level.WARNING, "Cannot clear update history for null report or report with empty ID.");
            return false;
        }
        String reportId = report.getReportId();
        File historyFile = new File(reportsDir, reportId + "_history.txt");
        if(historyFile.exists()) {
            return historyFile.delete();
        }
        return true;
    }
}
