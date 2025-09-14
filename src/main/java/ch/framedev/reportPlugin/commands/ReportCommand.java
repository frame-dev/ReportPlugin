package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.DiscordWebhook;
import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;

public record ReportCommand(ReportPlugin plugin, Database database) implements CommandExecutor {

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
                boolean enabledNotify = plugin.getConfig().getBoolean("notify.on-create", false);
                if (enabledNotify) {
                    onlinePlayer.sendMessage("§cNew report: §e" + player.getName() + " §areported §e" + reportedPlayer + " §afor: §f" + reason + ".");
                    boolean isHoverEnabled = plugin.getConfig().getBoolean("notify.hoverable-teleport", false);
                    if (isHoverEnabled) {
                        TextComponent textComponent = new TextComponent("§7[§eClick to Teleport to Report Location§7]");
                        Location location = Report.getLocationAsBukkitLocation(report.getLocation());
                        textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getX() + " " + location.getY() + " " + location.getZ()));
                        onlinePlayer.spigot().sendMessage(textComponent);
                    }
                }
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
        String contentText = plugin.getConfig().getString("discord.content", "New report received!");
        discordWebhook.setContent(contentText);
        String userName = plugin.getConfig().getString("discord.username", "ReportBot");
        discordWebhook.setUsername(userName);
        String avatarUrl = plugin.getConfig().getString("discord.avatar-url", "https://example.com/avatar.png");
        discordWebhook.setAvatarUrl(avatarUrl);
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        String embedTitle = plugin.getConfig().getString("discord.embed.title", "New Report");
        embedObject.setTitle(embedTitle);
        String description = plugin.getConfig().getString("discord.embed.description", "**Reported Player:** %ReportedPlayer%\\n**Reporter:** %Reporter%\\n**Reason:** %Reason%\\n**Server:** %ServerName%\\n**Location:** %Location%\\n**World:** %WorldName%");
        description = description.replace("%ReportedPlayer%", reportedPlayer)
                .replace("%Reporter%", player.getName())
                .replace("%Reason%", reason)
                .replace("%ServerName%", report.getServerName())
                .replace("%Location%", report.getLocation())
                .replace("%WorldName%", report.getWorldName());
        embedObject.setDescription(description);
        embedObject.setColor(Color.BLACK);
        String embedUrl = plugin.getConfig().getString("discord.embed.url", "https://example.com");
        embedObject.setUrl(embedUrl);
        String imageUrl = plugin.getConfig().getString("discord.embed.image.url", "https://example.com/image.png");
        embedObject.setImage(imageUrl);
        String thumbnailUrl = plugin.getConfig().getString("discord.embed.thumbnail.url", "https://example.com/thumbnail.png");
        embedObject.setThumbnail(thumbnailUrl);
        String footerText = plugin.getConfig().getString("discord.embed.footer.text", "Report ID: %ReporterID%");
        footerText = footerText.replace("%ReporterID%", report.getReportId());
        String avatarFooterUrl = plugin.getConfig().getString("discord.embed.footer.icon-url", "https://example.com/footer-icon.png");
        embedObject.setFooter(footerText, avatarFooterUrl);
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