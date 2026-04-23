package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.MessageUtils;
import ch.framedev.reportPlugin.utils.Report;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ReportClearUpdateHistoryCommand implements CommandExecutor, TabCompleter {

    private Database database;

    public ReportClearUpdateHistoryCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("report-clearupdatehistory")) {
            if (!sender.hasPermission("reportplugin.clearupdatehistory")) {
                MessageUtils.send(sender, "messages.no_permission", "&cYou do not have permission to use this command.");
                return true;
            }
            if (args.length != 1) {
                MessageUtils.send(sender, "messages.usage_report_clearupdatehistory", "&cUsage: /report-clearupdatehistory <reportId>");
                return true;
            }
            String reportId = args[0];
            if (database.clearUpdateHistory(database.getReportById(reportId))) {
                MessageUtils.send(sender, "messages.clear_update_history_success",
                        "&aUpdate history for report ID {reportId} has been cleared.",
                        "{reportId}", reportId);
            } else {
                MessageUtils.send(sender, "messages.clear_update_history_not_found",
                        "&cNo report found with ID {reportId}.",
                        "{reportId}", reportId);
            }
            return true;
        }
        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
            @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("report-clearupdatehistory")) {
            return null;
        }
        if (args.length == 1) {
            return database.getAllReports().stream()
                    .map(Report::getReportId)
                    .filter(s -> s.startsWith(args[0]))
                    .toList();
        }
        return List.of();
    }
}
