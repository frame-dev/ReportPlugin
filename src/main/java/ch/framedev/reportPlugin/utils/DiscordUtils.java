package ch.framedev.reportPlugin.utils;

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.awt.*;
import java.util.logging.Level;

public class DiscordUtils {

    /**
     * Sends a report to a Discord channel using a webhook.
     *
     * @param plugin the ReportPlugin instance containing configuration settings.
     * @param report the Report object containing details of the report.
     * @return true if the report was sent successfully, false otherwise.
     */
    public static boolean sendReportToDiscord(ReportPlugin plugin, Report report) {
        DiscordWebhook discordWebhook = new DiscordWebhook(plugin.getConfig().getString("discord.create.webhook-url"));
        String contentText = plugin.getConfig().getString("discord.create.content", "New report received!");
        discordWebhook.setContent(contentText);
        String userName = plugin.getConfig().getString("discord.create.username", "ReportBot");
        discordWebhook.setUsername(userName);
        String avatarUrl = plugin.getConfig().getString("discord.create.avatar-url", "https://example.com/avatar.png");
        discordWebhook.setAvatarUrl(avatarUrl);
        DiscordWebhook.EmbedObject embedObject = new DiscordWebhook.EmbedObject();
        String embedTitle = plugin.getConfig().getString("discord.create.embed.title", "New Report");
        embedObject.setTitle(embedTitle);
        String description = plugin.getConfig().getString("discord.create.embed.description", "**Reported Player:** %ReportedPlayer%\\n**Reporter:** %Reporter%\\n**Reason:** %Reason%\\n**Server:** %ServerName%\\n**Location:** %Location%\\n**World:** %WorldName%");
        description = description.replace("%ReportedPlayer%", report.getReportedPlayer())
                .replace("%Reporter%", report.getReporter())
                .replace("%Reason%", report.getReason())
                .replace("%ServerName%", report.getServerName() + ":" + plugin.getConfig().getString("server-address"))
                .replace("%Location%", report.getLocation())
                .replace("%WorldName%", report.getWorldName());
        embedObject.setDescription(description);
        embedObject.setColor(Color.BLACK);
        String embedUrl = plugin.getConfig().getString("discord.create.embed.url", "https://example.com");
        embedObject.setUrl(embedUrl);
        String imageUrl = plugin.getConfig().getString("discord.create.embed.image.url", "https://example.com/image.png");
        embedObject.setImage(imageUrl);
        String thumbnailUrl = plugin.getConfig().getString("discord.create.embed.thumbnail.url", "https://example.com/thumbnail.png");
        embedObject.setThumbnail(thumbnailUrl);
        String footerText = plugin.getConfig().getString("discord.create.embed.footer.text", "Report ID: %ReporterID%");
        footerText = footerText.replace("%ReporterID%", report.getReportId());
        String avatarFooterUrl = plugin.getConfig().getString("discord.create.embed.footer.icon-url", "https://example.com/footer-icon.png");
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

    /**
     * Sends a report update to a Discord channel using a webhook.
     *
     * @param report the Report object containing details of the report.
     * @return true if the report update was sent successfully, false otherwise.
     */
    public static boolean sendReportUpdateToDiscord(Report report) {
        // Implement Discord webhook update logic here
        FileConfiguration config = ReportPlugin.getInstance().getConfig();
        if (!config.getBoolean("useDiscordWebhook", false)) return false;
        String webhookUrl = config.getString("discord.update.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return false;
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl);
        discordWebhook.setUsername(config.getString("discord.update.username", "ReportBot"));
        discordWebhook.setAvatarUrl(config.getString("discord.update.avatar-url", "https://example.com/avatar.png"));
        discordWebhook.setContent(config.getString("discord.update.content", "Report updated!"));
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        embed.setTitle(config.getString("discord.update.embed.title", "Report Updated"));
        String description = config.getString("discord.update.embed.description", "");
        description = description.replace("%ReportedPlayer%", report.getReportedPlayer())
                .replace("%Reporter%", report.getReporter())
                .replace("%Reason%", report.getReason())
                .replace("%AdditionalInfo%", report.getAdditionalInfo() != null ? report.getAdditionalInfo() : "N/A")
                .replace("%Status%", report.isResolved() ? "Resolved" : "Unresolved")
                .replace("%ResolutionComment%", report.getResolutionComment() != null ? report.getResolutionComment() : "N/A")
                .replace("%ServerName%", report.getServerName() + ":" + ReportPlugin.getInstance().getConfig().getString("server-address"))
                .replace("%Location%", report.getLocation())
                .replace("%WorldName%", report.getWorldName());
        embed.setDescription(description);
        embed.setUrl(config.getString("discord.update.embed.url", "https://example.com"));
        embed.setColor(java.awt.Color.BLUE);
        embed.setFooter(config.getString("discord.update.embed.footer.text", "Report ID: %ReporterID%").replace("%ReporterID%", report.getReportId()),
                config.getString("discord.update.embed.footer.icon-url", "https://example.com/footer-icon.png"));
        embed.setImage(config.getString("discord.update.embed.image.url", "https://example.com/image.png"));
        embed.setThumbnail(config.getString("discord.update.embed.thumbnail.url", "https://example.com/thumbnail.png"));
        discordWebhook.addEmbed(embed);
        try {
            discordWebhook.execute();
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to send report update to Discord: " + e.getMessage());
            ReportPlugin.getInstance().getLogger().severe("Failed to send report update to Discord: " + e.getMessage());
        }
        return false;
    }

    /**
     * Sends a report resolution to a Discord channel using a webhook.
     *
     * @param report the Report object containing details of the report.
     * @return true if the report resolution was sent successfully, false otherwise.
     */
    public static boolean sendReportResolvedToDiscord(Report report) {
        // Implement Discord webhook update logic here
        FileConfiguration config = ReportPlugin.getInstance().getConfig();
        if (!config.getBoolean("useDiscordWebhook", false)) return false;
        String webhookUrl = config.getString("discord.resolved.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return false;
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl);
        discordWebhook.setUsername(config.getString("discord.resolved.username", "ReportBot"));
        discordWebhook.setAvatarUrl(config.getString("discord.resolved.avatar-url", "https://example.com/avatar.png"));
        discordWebhook.setContent(config.getString("discord.resolved.content", "Report Solved!"));
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        embed.setTitle(config.getString("discord.resolved.embed.title", "Report Solved"));
        String description = config.getString("discord.resolved.embed.description", "");
        description = description.replace("%ReportedPlayer%", report.getReportedPlayer())
                .replace("%Reporter%", report.getReporter())
                .replace("%Reason%", report.getReason())
                .replace("%AdditionalInfo%", report.getAdditionalInfo() != null ? report.getAdditionalInfo() : "N/A")
                .replace("%Status%", report.isResolved() ? "Resolved" : "Unresolved")
                .replace("%ResolutionComment%", report.getResolutionComment() != null ? report.getResolutionComment() : "N/A")
                .replace("%ServerName%", report.getServerName() + ":" + ReportPlugin.getInstance().getConfig().getString("server-address"))
                .replace("%Location%", report.getLocation())
                .replace("%WorldName%", report.getWorldName());
        embed.setDescription(description);
        embed.setUrl(config.getString("discord.resolved.embed.url", "https://example.com"));
        embed.setColor(java.awt.Color.BLUE);
        embed.setFooter(config.getString("discord.resolved.embed.footer.text", "Report ID: %ReporterID%").replace("%ReporterID%", report.getReportId()),
                config.getString("discord.resolved.embed.footer.icon-url", "https://example.com/footer-icon.png"));
        embed.setImage(config.getString("discord.resolved.embed.image.url", "https://example.com/image.png"));
        embed.setThumbnail(config.getString("discord.resolved.embed.thumbnail.url", "https://example.com/thumbnail.png"));
        discordWebhook.addEmbed(embed);
        try {
            discordWebhook.execute();
            return true;
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to send report resolved to Discord: " + e.getMessage());
            ReportPlugin.getInstance().getLogger().severe("Failed to send report resolved to Discord: " + e.getMessage());
        }
        return false;
    }
}
