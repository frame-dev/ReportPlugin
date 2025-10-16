package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.ChatColor;
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
            sender.sendMessage("§cOnly players can execute this command.");
            return true;
        }
        if (!player.hasPermission("reportplugin.updatehistory")) {
            player.sendMessage("§cYou do not have permission to execute this command.");
            return true;
        }
        if (args.length != 1) {
            player.sendMessage("§cUsage: /report-updatehistory <reportId>");
            return true;
        }
        String reportId = args[0];
        if (!database.reportExists(reportId)) {
            player.sendMessage("§cNo report found with ID: " + reportId);
            return true;
        }
        player.sendMessage("§aUpdate history for report ID: " + reportId);
        Map<String, Report> history = database.getUpdateHistory(database.getReportById(reportId));
        if (history.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No update history for this report.");
        } else {
            player.sendMessage(ChatColor.GREEN + "---- Update History for Report " + reportId + " ----");
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            history.forEach((updater, rep) -> {
                String updaterName = updater.replace("_-_", "");
                player.sendMessage(ChatColor.YELLOW + "Updated by: " + updaterName);
                player.sendMessage(ChatColor.YELLOW + "Reason: " + rep.getReason());
                player.sendMessage(ChatColor.YELLOW + "Reported Player: " + rep.getReportedPlayer());
                player.sendMessage(ChatColor.YELLOW + "Reporter: " + rep.getReporter());
                player.sendMessage(ChatColor.YELLOW + "Additional Info: " + (rep.getAdditionalInfo().isEmpty() ? "N/A" : rep.getAdditionalInfo()));
                player.sendMessage(ChatColor.YELLOW + "Resolved: " + (rep.isResolved() ? "Yes" : "No"));
                if (rep.isResolved()) {
                    player.sendMessage(ChatColor.YELLOW + "Resolution Comment: " + (rep.getResolutionComment().isEmpty() ? "N/A" : rep.getResolutionComment()));
                }
                player.sendMessage(ChatColor.YELLOW + "Time: " + sdf.format(new Date(rep.getTimestamp())));
                player.sendMessage(ChatColor.GRAY + "-----------------------------");
            });
            player.sendMessage(ChatColor.GREEN + "----------------------------------------");
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
