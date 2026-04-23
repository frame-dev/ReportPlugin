package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.MessageUtils;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class ReportListCommand implements CommandExecutor {

    private Database database;

    public ReportListCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("reportplugin.list")) {
            MessageUtils.send(sender, "messages.no_permission", "&cYou do not have permission to use this command.");
            return true;
        }

        List<Report> reports = database.getAllReports().stream()
                .filter(report -> !report.getStatus().isClosed())
                .toList();

        if (reports.isEmpty()) {
            MessageUtils.send(sender, "messages.report_list_empty", "&eThere are no reports.");
            return true;
        }
        if (reports.size() > 10) {
            MessageUtils.send(sender, "messages.report_list_use_gui", "&eUse /report-gui to view reports in a GUI.");
            return true;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        MessageUtils.send(sender, "messages.report_list_header", "&a---- Report List ----");
        for (Report report : reports) {
            MessageUtils.send(sender, "messages.report_list_id", "&bID: {reportId}", "{reportId}", report.getReportId());
            MessageUtils.send(sender, "messages.report_list_player", "&7Player: {player}", "{player}", report.getReportedPlayer());
            MessageUtils.send(sender, "messages.report_list_reporter", "&7Reporter: {reporter}", "{reporter}", report.getReporter());
            MessageUtils.send(sender, "messages.report_list_reason", "&7Reason: {reason}", "{reason}", report.getReason());
            MessageUtils.send(sender, "messages.report_list_timestamp", "&7Timestamp: {timestamp}",
                    "{timestamp}", sdf.format(new Date(report.getTimestamp())));
            MessageUtils.send(sender, "messages.report_list_status", "&eStatus: {status}",
                    "{status}", report.getStatus().getDisplayName());
            MessageUtils.send(sender, "messages.report_list_resolution", "&eResolution Comment: {resolution}",
                    "{resolution}", report.getResolutionComment());
            if (!report.getEvidenceUrl().isEmpty()) {
                MessageUtils.send(sender, "messages.report_list_evidence", "&eEvidence: {evidence}",
                        "{evidence}", report.getEvidenceUrl());
            }
        }
        MessageUtils.send(sender, "messages.report_list_footer", "&a----------------------");
        MessageUtils.send(sender, "messages.report_list_total", "&aTotal reports: {count}",
                "{count}", String.valueOf(reports.size()));
        return true;
    }
}
