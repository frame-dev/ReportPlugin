package ch.framedev.reportPlugin;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:09
 */

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.UUID;

public class ReportCommand implements CommandExecutor {

    private final ReportPlugin plugin;

    public ReportCommand(ReportPlugin plugin) {
        this.plugin = plugin;
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
        String reportedPlayer = args[0];
        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        sender.sendMessage("§aYou reported §e" + reportedPlayer + "§a for: §f" + reason);
        Report report = new Report(
                reportedPlayer,
                reason,
                player.getName(),
                UUID.randomUUID().toString(), // Generate a random report ID
                "SpigotTest Server", // Example server name
                "localhost",
                Bukkit.getVersion(),
                "world", // Example world name
                Report.getLocationAsString(player.getLocation())
        );
        sender.sendMessage(report.toString());
        Database database = new Database();
        database.insertReport(report);
        Bukkit.getLogger().info("Report created by " + player.getName() + ": " + report.getReportedPlayer() + " for reason: " + report.getReason());
        // Optionally, you can notify staff members
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("reportplugin.report.notify")) {
                onlinePlayer.sendMessage("§cNew report: §e" + player.getName() + " §areported §e" + reportedPlayer + " §afor: §f" + reason);
            }
        }
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
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to send report to Discord: " + e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}