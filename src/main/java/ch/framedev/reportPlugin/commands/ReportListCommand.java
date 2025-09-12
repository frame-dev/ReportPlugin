package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public record ReportListCommand(Database database) implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("reportplugin.list")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        List<Report> reports = database.getAllReports().stream().filter(report -> !report.isResolved()).toList();
        if (reports.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "There are no reports.");
            return true;
        }
        if (reports.size() > 10) {
            sender.sendMessage(ChatColor.YELLOW + "Use /reportgui to view reports in a GUI.");
            return true;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sender.sendMessage(ChatColor.GREEN + "---- Report List ----");
        for (Report report : reports) {
            sender.sendMessage(ChatColor.AQUA + "ID: " + report.getReportId());
            sender.sendMessage(ChatColor.GRAY + "Player: " + report.getReportedPlayer());
            sender.sendMessage(ChatColor.GRAY + "Reporter: " + report.getReporter());
            sender.sendMessage(ChatColor.GRAY + "Reason: " + report.getReason());
            sender.sendMessage(ChatColor.GRAY + "Timestamp: " + sdf.format(new Date(report.getTimestamp())));
            sender.sendMessage(ChatColor.YELLOW + "Resolved: " + (report.isResolved() ? "Yes" : "No"));
            sender.sendMessage(ChatColor.YELLOW + "Resolution Comment: " + report.getResolutionComment());
        }
        sender.sendMessage(ChatColor.GREEN + "----------------------");
        sender.sendMessage(ChatColor.GREEN + "Total reports: " + reports.size());
        return true;
    }
}