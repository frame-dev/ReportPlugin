package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class FileSystemHelper implements DatabaseHelper {

    public FileSystemHelper(ReportPlugin plugin) {
    }

    @Override
    public void insertReport(Report report) {
        File file = new File(ReportPlugin.getInstance().getDataFolder(), "reports");
        if (!file.exists()) {
            file.mkdirs();
        }
        try (FileWriter writer = new FileWriter(new File(file, "report_" + report.getReportedPlayer() + ".json"))) {
            writer.write(new Gson().toJson(this));
        } catch (Exception e) {
            e.printStackTrace();
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
                    e.printStackTrace();
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
                file.delete();
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
