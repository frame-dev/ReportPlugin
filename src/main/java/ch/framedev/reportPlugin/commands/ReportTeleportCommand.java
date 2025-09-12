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

public record ReportTeleportCommand(Database database) implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }
        if (args.length != 2) {
            player.sendMessage("Usage: /reporttp <player> <reportID>");
            return true;
        }
        if (!player.hasPermission("reportplugin.reporttp")) {
            player.sendMessage("You do not have permission to use this command.");
            return true;
        }
        String reportedPlayerName = args[0];
        String reportId = args[1];
        Report report = database.getReportById(reportId);
        if (report != null && report.getReportedPlayer().equalsIgnoreCase(reportedPlayerName)) {
            Player reportedPlayer = player.getServer().getPlayerExact(reportedPlayerName);
            if (reportedPlayer != null) {
                player.teleport(reportedPlayer.getLocation());
                player.sendMessage("Teleported to " + reportedPlayerName + " for report ID: " + reportId);
            } else {
                player.sendMessage("Player " + reportedPlayerName + " is not online.");
            }
        } else {
            player.sendMessage("No report found with ID: " + reportId + " for player: " + reportedPlayerName);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("reporttp")) {
            return null;
        }
        if (args.length == 1) {
            return database.getAllReports().stream().map(Report::getReportedPlayer).toList();
        } else if (args.length == 2) {
            String reportedPlayerName = args[0];
            return database.getAllReports().stream().filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayerName)).map(Report::getReportId).toList();
        }
        return List.of();
    }
}
