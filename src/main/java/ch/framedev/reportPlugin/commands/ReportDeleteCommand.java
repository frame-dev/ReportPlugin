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

import java.util.List;

public class ReportDeleteCommand implements CommandExecutor, TabCompleter {

    private Database database;

    public ReportDeleteCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.send(sender, "messages.only_players_delete", "&cOnly players can execute this command!");
            return true;
        }
        if (!player.hasPermission("reportplugin.reportdelete")) {
            MessageUtils.send(player, "messages.no_permission_delete", "&cYou don't have permission to execute this command!");
            return true;
        }
        if (args.length != 1) {
            MessageUtils.send(player, "messages.usage_report_delete", "&cUsage: /report-delete <reportId>");
            return true;
        }
        String reportId = args[0];
        if (!database.deleteReport(reportId)) {
            MessageUtils.send(player, "messages.report_delete_not_found", "&cNo report found with ID {reportId}.", "{reportId}", reportId);
            return true;
        } else {
            MessageUtils.send(player, "messages.report_delete_success", "&aReport with ID {reportId} has been deleted.", "{reportId}", reportId);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("report-delete")) {
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
