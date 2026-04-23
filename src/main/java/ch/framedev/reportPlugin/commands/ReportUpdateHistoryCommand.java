package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.MessageUtils;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportUpdateHistoryCommand implements CommandExecutor, TabCompleter {

    private Database database;

    public ReportUpdateHistoryCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.send(sender, "messages.only_players_update_history", "&cOnly players can execute this command.");
            return true;
        }
        if (!player.hasPermission("reportplugin.updatehistory")) {
            MessageUtils.send(player, "messages.no_permission_update_history", "&cYou do not have permission to execute this command.");
            return true;
        }
        if (args.length != 1) {
            MessageUtils.send(player, "messages.usage_report_updatehistory", "&cUsage: /report-updatehistory <reportId>");
            return true;
        }
        String reportId = args[0];
        if (!database.reportExists(reportId)) {
            MessageUtils.send(player, "messages.report_updatehistory_not_found", "&cNo report found with ID: {reportId}", "{reportId}", reportId);
            return true;
        }
        MessageUtils.send(player, "messages.report_updatehistory_intro", "&aUpdate history for report ID: {reportId}", "{reportId}", reportId);
        Map<String, Report> history = database.getUpdateHistory(database.getReportById(reportId));
        if (history.isEmpty()) {
            MessageUtils.send(player, "messages.update_history_empty", "&eNo update history for this report.");
        } else {
            MessageUtils.send(player, "messages.update_history_header", "&a---- Update History for Report {reportId} ----", "{reportId}", reportId);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            history.forEach((updater, rep) -> {
                String updaterName = updater.replace("_-_", "");
                MessageUtils.send(player, "messages.update_history_updated_by", "&eUpdated by: {updater}", "{updater}", updaterName);
                MessageUtils.send(player, "messages.update_history_reason", "&eReason: {reason}", "{reason}", rep.getReason());
                MessageUtils.send(player, "messages.update_history_reported_player", "&eReported Player: {player}",
                        "{player}", rep.getReportedPlayer());
                MessageUtils.send(player, "messages.update_history_reporter", "&eReporter: {reporter}",
                        "{reporter}", rep.getReporter());
                MessageUtils.send(player, "messages.update_history_status", "&eStatus: {status}",
                        "{status}", rep.getStatus().getDisplayName());
                MessageUtils.send(player, "messages.update_history_staff_notes", "&eStaff Notes: {notes}",
                        "{notes}", rep.getAdditionalInfo().isEmpty() ? "N/A" : rep.getAdditionalInfo());
                if (!rep.getEvidenceUrl().isEmpty()) {
                    MessageUtils.send(player, "messages.update_history_evidence", "&eEvidence: {evidence}",
                            "{evidence}", rep.getEvidenceUrl());
                }
                if (rep.getStatus().isClosed()) {
                    MessageUtils.send(player, "messages.update_history_resolution", "&eResolution Comment: {resolution}",
                            "{resolution}", rep.getResolutionComment().isEmpty() ? "N/A" : rep.getResolutionComment());
                }
                MessageUtils.send(player, "messages.update_history_time", "&eTime: {time}",
                        "{time}", sdf.format(new Date(rep.getTimestamp())));
                MessageUtils.send(player, "messages.update_history_separator", "&7-----------------------------");
            });
            MessageUtils.send(player, "messages.update_history_footer", "&a----------------------------------------");
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("report-updatehistory")) {
            return null;
        }
        if (args.length == 1) {
            return database.getAllReports().stream()
                    .map(Report::getReportId)
                    .filter(id -> id.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}
