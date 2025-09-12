package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public record ReportDeleteCommand(Database database) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can execute this command!");
            return true;
        }
        if (!player.hasPermission("reportplugin.reportdelete")) {
            player.sendMessage("§cYou don't have permission to execute this command!");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("§cUsage: /report-delete <reportId>");
            return true;
        }
        String reportId = args[0];
        if (!database.deleteReport(reportId)) {
            player.sendMessage("§cNo report found with ID " + reportId + ".");
            return true;
        } else
            player.sendMessage("§aReport with ID " + reportId + " has been deleted.");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("report-delete")) {
            return null;
        }
        if (args.length == 1) {
            return database.getAllReports().stream().map(Report::getReportId).toList();
        }
        return List.of();
    }
}
