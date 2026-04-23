package ch.framedev.reportPlugin.utils;

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;

public record ConfigUtils(FileConfiguration config) {

    public void initializeConfig(ReportPlugin plugin) {
        setupConfig();
        plugin.saveConfig();
    }

    private void setupConfig() {
        setDefault("server-name", "Localhost Server");
        setComment("server-name", "This name will be displayed in the Discord webhook and saved in the database.");

        setDefault("server-address", "localhost");
        setComment("server-address", "This address will be displayed in the Discord webhook and saved in the database.");

        setDefault("bungeecord", false);
        setComment("bungeecord", "Set to true if you are using Bungeecord or Waterfall.");

        setDefault("useDiscordWebhook", false);
        setComment("useDiscordWebhook", "Set to true to enable Discord webhook notifications for reports.");

        setDefault("report-settings.max-reports-per-player", 3);
        setDefault("report-settings.max-reports-per-reporter", 10);
        setDefault("report-settings.duplicate-window-seconds", 300);
        setComment("report-settings", "Settings related to reporting limits.");

        setDefault("notify.on-create", true);
        setDefault("notify.on-update", false);
        setDefault("notify.on-resolve", false);
        setDefault("notify.hoverable-teleport", true);
        setComment("notify", "Settings related to in-game staff notifications.");

        setupDiscordWebhookConfig();
        setupDatabaseConfig();
    }

    private void setupDiscordWebhookConfig() {
        setComment("discord", "Configure the Discord webhook integration below.");

        setDefault("discord.create.webhook-url", "YOUR_WEBHOOK_URL_HERE");
        setDefault("discord.create.username", "ReportBot");
        setDefault("discord.create.avatar-url", "https://example.com/avatar.png");
        setDefault("discord.create.content", "New report received!");
        setDefault("discord.create.embed.title", "New Report");
        setDefault("discord.create.embed.description",
                "**Reported Player:** %ReportedPlayer%\\n"
                        + "**Reporter:** %Reporter%\\n"
                        + "**Reason:** %Reason%\\n"
                        + "**Status:** %Status%\\n"
                        + "**Staff Notes:** %AdditionalInfo%\\n"
                        + "**Evidence:** %EvidenceUrl%\\n"
                        + "**Server:** %ServerName%\\n"
                        + "**Location:** %Location%\\n"
                        + "**World:** %WorldName%");
        setDefault("discord.create.embed.url", "https://example.com");
        setDefault("discord.create.embed.footer.text", "Report ID: %ReporterID%");
        setDefault("discord.create.embed.footer.icon-url", "https://example.com/footer-icon.png");
        setDefault("discord.create.embed.image.url", "https://example.com/image.png");
        setDefault("discord.create.embed.thumbnail.url", "https://example.com/thumbnail.png");

        setDefault("discord.update.webhook-url", "YOUR_WEBHOOK_URL_HERE");
        setDefault("discord.update.username", "ReportBot");
        setDefault("discord.update.avatar-url", "https://example.com/avatar.png");
        setDefault("discord.update.content", "Report updated!");
        setDefault("discord.update.embed.title", "Report Updated");
        setDefault("discord.update.embed.description",
                "**Reported Player:** %ReportedPlayer%\\n"
                        + "**Reporter:** %Reporter%\\n"
                        + "**Reason:** %Reason%\\n"
                        + "**Status:** %Status%\\n"
                        + "**Staff Notes:** %AdditionalInfo%\\n"
                        + "**Evidence:** %EvidenceUrl%\\n"
                        + "**Resolution Comment:** %ResolutionComment%\\n"
                        + "**Server:** %ServerName%\\n"
                        + "**Location:** %Location%\\n"
                        + "**World:** %WorldName%");
        setDefault("discord.update.embed.url", "https://example.com");
        setDefault("discord.update.embed.footer.text", "Report ID: %ReporterID%");
        setDefault("discord.update.embed.footer.icon-url", "https://example.com/footer-icon.png");
        setDefault("discord.update.embed.image.url", "https://example.com/image.png");
        setDefault("discord.update.embed.thumbnail.url", "https://example.com/thumbnail.png");

        setDefault("discord.resolved.webhook-url", "YOUR_WEBHOOK_URL_HERE");
        setDefault("discord.resolved.username", "ReportBot");
        setDefault("discord.resolved.avatar-url", "https://example.com/avatar.png");
        setDefault("discord.resolved.content", "Report Closed!");
        setDefault("discord.resolved.embed.title", "Report Closed");
        setDefault("discord.resolved.embed.description",
                "**Reported Player:** %ReportedPlayer%\\n"
                        + "**Reporter:** %Reporter%\\n"
                        + "**Reason:** %Reason%\\n"
                        + "**Status:** %Status%\\n"
                        + "**Staff Notes:** %AdditionalInfo%\\n"
                        + "**Evidence:** %EvidenceUrl%\\n"
                        + "**Resolution Comment:** %ResolutionComment%\\n"
                        + "**Server:** %ServerName%\\n"
                        + "**Location:** %Location%\\n"
                        + "**World:** %WorldName%");
        setDefault("discord.resolved.embed.url", "https://example.com");
        setDefault("discord.resolved.embed.footer.text", "Report ID: %ReporterID%");
        setDefault("discord.resolved.embed.footer.icon-url", "https://example.com/footer-icon.png");
        setDefault("discord.resolved.embed.image.url", "https://example.com/image.png");
        setDefault("discord.resolved.embed.thumbnail.url", "https://example.com/thumbnail.png");

        setDefault("discord.notify.on-create", true);
        setDefault("discord.notify.on-update", true);
        setDefault("discord.notify.on-resolve", false);
        setDefault("discord.notify.hoverable-teleport", true);
    }

    private void setupDatabaseConfig() {
        setComment("mysql", "Make sure to have a running MySQL instance.");
        setDefault("mysql.host", "localhost");
        setDefault("mysql.port", 3306);
        setDefault("mysql.database", "reports");
        setDefault("mysql.username", "yourUsername");
        setDefault("mysql.password", "yourPassword");

        setComment("postgresql", "Make sure to have a running PostgreSQL instance.");
        setDefault("postgresql.host", "localhost");
        setDefault("postgresql.port", 5432);
        setDefault("postgresql.database", "reports");
        setDefault("postgresql.username", "yourUsername");
        setDefault("postgresql.password", "yourPassword");

        setComment("sqlite", "The database file will be created in the plugin's data folder.");
        setDefault("sqlite.file", "reports.db");
        setDefault("sqlite.path", "database");
        setDefault("sqlite.database", "reports.db");

        setComment("h2Storage", "The database file will be created in the plugin's data folder.");
        setDefault("h2Storage.file", "reports");
        setDefault("h2Storage.path", "database");

        setComment("mongodb", "Make sure to have a running MongoDB instance.");
        setDefault("mongodb.host", "localhost");
        setDefault("mongodb.port", 27017);
        setDefault("mongodb.database", "reports");
        setDefault("mongodb.username", "yourUsername");
        setDefault("mongodb.password", "yourPassword");

        setComment("redis", "Make sure to have a running Redis instance before enabling cache support.");
        setDefault("redis.enabled", false);
        setDefault("redis.host", "localhost");
        setDefault("redis.port", 6379);
        setDefault("redis.password", "");
        setDefault("redis.ttl", 300);

        setDefault("database", "jsonfilesystem");
        setComment("database", "Supported values: mysql, sqlite, postgresql, h2, mongodb, jsonfilesystem, yamlfilesystem, textfilesystem");
    }

    private void setDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    private void setComment(String path, String comment) {
        config.setComments(path, Collections.singletonList(comment));
    }
}
