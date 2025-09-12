package ch.framedev.reportPlugin.database;

import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;

public class FileSystemHelper implements DatabaseHelper {

    private final ReportPlugin plugin;
    private final File reportsDir;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Initializes the FileSystemHelper with the specified ReportPlugin instance.
     * Ensures that the reports directory exists.
     *
     * @param plugin The ReportPlugin instance for logging and configuration access.
     */
    public FileSystemHelper(ReportPlugin plugin) {
        this.plugin = plugin;
        this.reportsDir = new File(plugin.getDataFolder(), "reports");
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create reports directory: " + reportsDir.getAbsolutePath());
        }
        plugin.getLogger().info("FileSystemHelper initialized. Reports directory: " + reportsDir.getAbsolutePath());
    }

    // ---- Helpers ------------------------------------------------------------

    private File fileForReportId(String reportId) {
        return new File(reportsDir, "report_" + reportId + ".json");
    }

    private void writeReportFile(File target, Report report) throws IOException {
        // Write to a temp file then move into place for atomic-ish replace
        File tmp = File.createTempFile("report_", ".json", reportsDir);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(tmp), StandardCharsets.UTF_8)) {
            gson.toJson(report, w);
        }
        try {
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            // Fallback if filesystem doesn't support atomic move
            Files.move(tmp.toPath(), target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private Report readReportFile(File file) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return gson.fromJson(r, Report.class);
        }
    }

    // ---- CRUD ---------------------------------------------------------------

    @Override
    public void insertReport(Report report) {
        if (report == null) {
            plugin.getLogger().log(Level.SEVERE, "insertReport called with null report");
            return;
        }
        String reportId = report.getReportId();
        if (reportId == null || reportId.isEmpty()) {
            plugin.getLogger().log(Level.SEVERE, "Report has no reportId; cannot save.");
            return;
        }
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create reports directory!");
            return;
        }
        File out = fileForReportId(reportId);
        try {
            writeReportFile(out, report);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save report " + reportId + " (player " + report.getReportedPlayer() + ")", e);
        }
    }

    @Override
    public Report getReportById(String reportId) {
        if (reportId == null || reportId.isEmpty()) return null;
        File f = fileForReportId(reportId);
        if (!f.exists()) return null;
        try {
            return readReportFile(f);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Could not read report: " + f.getName(), e);
            return null;
        }
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        // Return the first match (or you could choose newest)
        for (Report r : getAllReports()) {
            if (Objects.equals(r.getReportedPlayer(), reportedPlayer)) return r;
        }
        return null;
    }

    @Override
    public Report getReportByReporter(String reporter) {
        for (Report r : getAllReports()) {
            if (Objects.equals(r.getReporter(), reporter)) return r;
        }
        return null;
    }

    @Override
    public List<Report> getAllReports() {
        List<Report> reports = new ArrayList<>();
        if (!reportsDir.exists()) return reports;

        File[] files = reportsDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) return reports;

        for (File f : files) {
            try {
                Report report = readReportFile(f);
                if (report != null) reports.add(report);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Could not read report file: " + f.getName(), e);
            }
        }
        return reports;
    }

    @Override
    public void updateReport(Report report) {
        // overwrite the same ID file
        insertReport(report);
    }

    @Override
    public boolean deleteReport(String reportId) {
        if (reportId == null || reportId.isEmpty()) return false;
        File f = fileForReportId(reportId);
        if (f.exists() && !f.delete()) {
            plugin.getLogger().log(Level.SEVERE, "Could not delete report file: " + f.getAbsolutePath());
            return false;
        }
        return true;
    }

    @Override
    public void createTable() {
        // Not needed for filesystem backend; ensure directory exists
        if (!reportsDir.exists() && !reportsDir.mkdirs()) {
            plugin.getLogger().log(Level.SEVERE, "Could not create reports directory!");
        }
    }

    @Override
    public Report getReportByPlayer(String reportedPlayer) {
        return getReportByReportedPlayer(reportedPlayer);
    }

    @Override
    public boolean reportExists(String reportId) {
        return reportId != null && !reportId.isEmpty() && fileForReportId(reportId).exists();
    }

    @Override
    public boolean playerHasReport(String reportedPlayer) {
        // Simple scan — you could add an index if this grows large
        return getReportByReportedPlayer(reportedPlayer) != null;
    }

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        int count = 0;
        for (Report r : getAllReports()) {
            if (Objects.equals(r.getReportedPlayer(), reportedPlayer)) count++;
        }
        return count;
    }

    @Override
    public boolean connect() {
        // Always "connected" for filesystem
        return true;
    }
}