package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class FileSystemHelper implements DatabaseHelper {

    public FileSystemHelper(ReportPlugin plugin) {
        // Ensure the reports directory exists
        File file = new File(plugin.getDataFolder(), "reports");
        if (!file.exists()) {
            if(!file.mkdirs()) {
                plugin.getLogger().log(Level.SEVERE, "Could not create reports directory!");
            }
        }
    }

    @Override
    public void insertReport(Report report) {
        File file = new File(ReportPlugin.getInstance().getDataFolder(), "reports");
        if (!file.exists()) {
            if(!file.mkdirs()) {
                ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not create reports directory!");
                return;
            }
        }
        try (FileWriter writer = new FileWriter(new File(file, "report_" + report.getReportedPlayer() + ".json"))) {
            writer.write(new Gson().toJson(this));
        } catch (Exception e) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not save report for player " + report.getReportedPlayer(), e);
        }
    }

    @Override
    public Report getReportById(String reportId) {
        for(Report report : getAllReports()) {
            if(report.getReportId().equals(reportId)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        for(Report report : getAllReports()) {
            if(report.getReportedPlayer().equals(reportedPlayer)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public Report getReportByReporter(String reporter) {
        for(Report report : getAllReports()) {
            if(report.getReporter().equals(reporter)) {
                return report;
            }
        }
        return null;
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        File file = new File(ReportPlugin.getInstance().getDataFolder(), "reports");
        if (!file.exists()) {
            return reports;
        }
        File[] files = file.listFiles((dir, name) -> name.endsWith(".json"));
        if (files != null) {
            for (File f : files) {
                try {
                    Report report = new Gson().fromJson(new String(java.nio.file.Files.readAllBytes(f.toPath())), Report.class);
                    reports.add(report);
                } catch (Exception e) {
                    ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not read report file: " + f.getName(), e);
                }
            }
        }
        return reports;
    }

    @Override
    public void updateReport(Report report) {
        insertReport(report);
    }

    @Override
    public void deleteReport(String reportId) {
        Report report = getReportById(reportId);
        if (report != null) {
            File file = new File(ReportPlugin.getInstance().getDataFolder(), "reports/report_" + report.getReportedPlayer() + ".json");
            if (file.exists()) {
                if(!file.delete()) {
                    ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Could not delete report file for player " + report.getReportedPlayer());
                }
            }
        }
    }

    @Override
    public void createTable() {

    }

    @Override
    public Report getReportByPlayer(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer);
    }

    @Override
    public boolean reportExists(String reportId) {
        return getReportById(reportId) != null;
    }

    @Override
    public boolean playerHasReport(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer) != null;
    }
}
