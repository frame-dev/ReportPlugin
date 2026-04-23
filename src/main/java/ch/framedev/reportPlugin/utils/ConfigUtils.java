package ch.framedev.reportPlugin.utils;

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public record ConfigUtils(FileConfiguration config) {

    public void initializeConfig(ReportPlugin plugin) {
        config.options().parseComments(true);
        setupConfig();
        plugin.saveConfig();
    }

    private void setupConfig() {
        setDefault("server-name", "Localhost Server");
        setComments("server-name", "Basic server information stored with each report and shown in Discord messages.");

        setDefault("server-address", "localhost");

        setDefault("bungeecord", false);
        setComments("bungeecord", "Enable this if your server runs behind BungeeCord/Waterfall/Velocity-style proxy handling.");

        setDefault("useDiscordWebhook", false);
        setComments("useDiscordWebhook", "Master switch for all Discord webhook messages below.");

        setDefault("report-settings.max-reports-per-player", 3);
        setComments("report-settings", "Limits for how often players can create reports.");
        setComments("report-settings.max-reports-per-player",
                "Maximum number of reports that can exist for one reported player. Set to 0 to remove the limit.");
        setDefault("report-settings.max-reports-per-reporter", 10);
        setComments("report-settings.max-reports-per-reporter",
                "Maximum number of reports one reporter can create. Set to 0 to remove the limit.");
        setDefault("report-settings.duplicate-window-seconds", 300);
        setComments("report-settings.duplicate-window-seconds",
                "Cooldown in seconds before the same reporter can report the same target again.");

        setDefault("notify.on-create", true);
        setComments("notify", "In-game staff notifications.");
        setComments("notify.on-create",
                "Notify staff with `reportplugin.report.notify` when a new report is created.");
        setDefault("notify.on-update", false);
        setComments("notify.on-update", "Notify staff when a report is edited or updated.");
        setDefault("notify.on-resolve", false);
        setComments("notify.on-resolve",
                "Notify staff when a report reaches a closed state such as resolved/rejected/punished.");
        setDefault("notify.hoverable-teleport", true);
        setComments("notify.hoverable-teleport", "Adds the clickable teleport hint in create notifications.");

        setupDiscordWebhookConfig();
        setupDatabaseConfig();
    }

    private void setupDiscordWebhookConfig() {
        setComments("discord",
                "Discord webhook configuration.",
                "Placeholders you can use in embed text:",
                "%ReportedPlayer%, %Reporter%, %Reason%, %Status%, %AdditionalInfo%, %EvidenceUrl%,",
                "%ResolutionComment%, %ServerName%, %Location%, %WorldName%, %ReporterID%");

        setComments("discord.create", "Webhook payload used when a report is first created.");
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
        setComments("discord.create.embed.url", "Optional link attached to the embed title.");
        setDefault("discord.create.embed.footer.text", "Report ID: %ReporterID%");
        setDefault("discord.create.embed.footer.icon-url", "https://example.com/footer-icon.png");
        setDefault("discord.create.embed.image.url", "https://example.com/image.png");
        setDefault("discord.create.embed.thumbnail.url", "https://example.com/thumbnail.png");

        setComments("discord.update", "Webhook payload used when staff update an existing report.");
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

        setComments("discord.resolved", "Webhook payload used when a report is closed or otherwise resolved.");
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

        setComments("discord.notify", "Toggles which Discord events are sent when `useDiscordWebhook` is true.");
        setDefault("discord.notify.on-create", true);
        setDefault("discord.notify.on-update", true);
        setDefault("discord.notify.on-resolve", false);
        setDefault("discord.notify.hoverable-teleport", true);
        setComments("discord.notify.hoverable-teleport",
                "Present in the config template for parity with in-game notify settings.");
    }

    private void setupDatabaseConfig() {
        setComments("mysql", "MySQL connection settings.");
        setDefault("mysql.host", "localhost");
        setDefault("mysql.port", 3306);
        setDefault("mysql.database", "reports");
        setDefault("mysql.username", "yourUsername");
        setDefault("mysql.password", "yourPassword");

        setComments("postgresql", "PostgreSQL connection settings.");
        setDefault("postgresql.host", "localhost");
        setDefault("postgresql.port", 5432);
        setDefault("postgresql.database", "reports");
        setDefault("postgresql.username", "yourUsername");
        setDefault("postgresql.password", "yourPassword");

        setComments("sqlite", "SQLite stores the database in a local file inside the plugin folder.");
        setDefault("sqlite.file", "reports.db");
        setComments("sqlite.file", "Legacy filename entry kept in the template.");
        setDefault("sqlite.path", "database");
        setComments("sqlite.path", "Relative folder path for the SQLite database file.");
        setDefault("sqlite.database", "reports.db");
        setComments("sqlite.database", "Actual SQLite database filename used by the plugin.");

        setComments("h2Storage", "H2 local file storage settings.");
        setDefault("h2Storage.file", "reports");
        setDefault("h2Storage.path", "database");

        setComments("mongodb", "MongoDB connection settings.");
        setDefault("mongodb.host", "localhost");
        setDefault("mongodb.port", 27017);
        setDefault("mongodb.database", "reports");
        setDefault("mongodb.username", "yourUsername");
        setDefault("mongodb.password", "yourPassword");

        setComments("redis", "Optional Redis cache used by the MySQL backend.");
        setDefault("redis.enabled", false);
        setDefault("redis.host", "localhost");
        setDefault("redis.port", 6379);
        setDefault("redis.password", "");
        setDefault("redis.ttl", 300);
        setComments("redis.ttl", "Cache TTL in seconds.");

        setDefault("database", "jsonfilesystem");
        setComments("database",
                "Storage backend to use.",
                "Supported values: mysql, sqlite, postgresql, h2, mongodb, jsonfilesystem, yamlfilesystem, textfilesystem");
    }

    private void setDefault(String path, Object value) {
        if (!config.contains(path)) {
            config.set(path, value);
        }
    }

    private void setComments(String path, String... comments) {
        config.setComments(path, List.of(comments));
    }
}
