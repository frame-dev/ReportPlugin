package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.DiscordWebhook;
import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public class ReportCommand implements CommandExecutor {

    private final ReportPlugin plugin;
    private final Database database;

    public ReportCommand(ReportPlugin plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + label + " <player> [reason]");
            return true;
        }
        if (!sender.hasPermission("reportplugin.report")) {
            sender.sendMessage("§cYou do not have permission to report players.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        report(args, sender, player);
        return true;
    }

    private void report(String[] args, CommandSender sender, Player player) {
        String reportedPlayer = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        sender.sendMessage("§aYou reported §e" + reportedPlayer + "§a for: §f" + reason);
        Report report = new Report(
                reportedPlayer,
                reason,
                player.getName(),
                UUID.randomUUID().toString(),
                plugin.getConfig().getString("server-name", "Localhost"),
                plugin.getConfig().getString("server-address", "localhost"),
                Bukkit.getVersion(),
                "world",
                Report.getLocationAsString(player.getLocation())
        );
        database.insertReport(report);
        Bukkit.getLogger().info("Report created by " + player.getName() + ": " + report.getReportedPlayer() + " for reason: " + report.getReason());
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("reportplugin.report.notify")) {
                onlinePlayer.sendMessage("§cNew report: §e" + player.getName() + " §areported §e" + reportedPlayer + " §afor: §f" + reason);
            }
        }
        if (plugin.getConfig().getBoolean("useDiscordWebhook", false)) {
            if (!sendReportToDiscord(player, reportedPlayer, reason, report)) {
                Bukkit.getLogger().severe("Failed to send report to Discord.");
            } else {
                Bukkit.getLogger().info("Report sent to Discord successfully.");
            }
        }
    }

    private boolean sendReportToDiscord(Player player, String reportedPlayer, String reason, Report report) {
        DiscordWebhook discordWebhook = new DiscordWebhook(plugin.getConfig().getString("discord.webhook-url"));
        discordWebhook.setContent("New report received!");
        discordWebhook.setUsername("ReportBot");
        discordWebhook.setAvatarUrl("https://example.com/avatar.png");
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        embedObject.setTitle("New Report");
        String description = "**Reported Player:** " + reportedPlayer + "\\n" +
                             "**Reporter:** " + player.getName() + "\\n" +
                             "**Reason:** " + reason + "\\n" +
                             "**Server:** " + report.getServerName() + "\\n" +
                             "**Location:** " + report.getLocation() + "\\n" +
                             "**World:** " + report.getWorldName();
        embedObject.setDescription(description);
        embedObject.setColor(Color.BLACK);
        embedObject.setFooter("Report ID: " + report.getReportId(), "https://example.com/avatar.png");
        discordWebhook.addEmbed(embedObject);
        try {
            discordWebhook.execute();
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to send report to Discord: " + e.getMessage());
            plugin.getLogger().log(Level.SEVERE, "Failed to send report to Discord: " + e.getMessage(), e);
            return false;
        }
    }
}