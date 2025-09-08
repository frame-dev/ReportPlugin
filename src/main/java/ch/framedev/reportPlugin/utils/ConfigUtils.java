package ch.framedev.reportPlugin.utils;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:04
 */

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Collections;

public record ConfigUtils(FileConfiguration config) {

    public void initializeConfig(ReportPlugin plugin) {
        setupConfig(plugin);
        plugin.saveConfig();
    }

    private void setupConfig(ReportPlugin plugin) {
        if (!config.contains("discord")) {
            ConfigurationSection section = config.createSection("discord");
            section.setComments("discord", Collections.singletonList("Make sure to set up a Discord Webhook!"));
            config.set("discord", section);
            ConfigurationSection createSection = config.createSection("discord.create");
            createSection.set("webhook-url", "YOUR_WEBHOOK_URL_HERE");
            createSection.set("username", "ReportBot");
            createSection.set("avatar-url", "https://example.com/avatar.png");
            createSection.set("content", "New report received!");
            createSection.set("embed.title", "New Report");
            createSection.set("embed.description", "**Reported Player:** %ReportedPlayer%\\n" +
                    "**Reporter:** %Reporter%\\n" +
                    "**Reason:** %Reason%\\n" +
                    "**Server:** %ServerName%\\n" +
                    "**Location:** %Location%\\n" +
                    "**World:** %WorldName%");
            createSection.set("embed.url", "https://example.com");
            createSection.set("embed.footer.text", "Report ID: %ReporterID%");
            createSection.set("embed.footer.icon-url", "https://example.com/footer-icon.png");
            createSection.set("embed.image.url", "https://example.com/image.png");
            createSection.set("embed.thumbnail.url", "https://example.com/thumbnail.png");
            config.set("discord.create", createSection);

            ConfigurationSection updatedSection = config.createSection("discord.update");
            updatedSection.set("webhook-url", "YOUR_WEBHOOK_URL_HERE");
            updatedSection.set("username", "ReportBot");
            updatedSection.set("avatar-url", "https://example.com/avatar.png");
            updatedSection.set("content", "Report updated!");
            updatedSection.set("embed.title", "Report Updated");
            updatedSection.set("embed.description", "**Reported Player:** %ReportedPlayer%\\n" +
                    "**Reporter:** %Reporter%\\n" +
                    "**Reason:** %Reason%\\n" +
                    "**Status:** %Status%\\n" +
                    "**Additional Info:** %AdditionalInfo%\\n" +
                    "**Resolution Comment:** %ResolutionComment%\\n" +
                    "**Server:** %ServerName%\\n" +
                    "**Location:** %Location%\\n" +
                    "**World:** %WorldName%");
            updatedSection.set("embed.url", "https://example.com");
            updatedSection.set("embed.footer.text", "Report ID: %ReporterID%");
            updatedSection.set("embed.footer.icon-url", "https://example.com/footer-icon.png");
            updatedSection.set("embed.image.url", "https://example.com/image.png");
            updatedSection.set("embed.thumbnail.url", "https://example.com/thumbnail.png");
            config.set("discord.update", updatedSection);
        }

        if (!config.contains("mysql")) {
            ConfigurationSection section = config.createSection("mysql");
            section.setComments("mysql", Collections.singletonList("Make sure to have a running MySQL instance!"));
            section.set("host", "localhost");
            section.set("port", 3306);
            section.set("database", "reports");
            section.set("username", "yourUsername");
            section.set("password", "yourPassword");
            config.set("mysql", section);
        }

        if (!config.contains("sqlite")) {
            ConfigurationSection section = config.createSection("sqlite");
            section.setComments("sqlite", Collections.singletonList("The database file will be created in the plugin's data folder."));
            section.set("file", "reports.db");
            section.set("path", "database");
            config.set("sqlite", section);
        }

        if (!config.contains("mongodb")) {
            ConfigurationSection section = config.createSection("mongodb");
            section.setComments("mongodb", Collections.singletonList("Make sure to have a running MongoDB instance!"));
            section.set("host", "localhost");
            section.set("port", 27017);
            section.set("database", "spigotTestDB");
            section.set("username", "yourUsername");
            section.set("password", "yourPassword");
            config.set("mongodb", section);
        }

        if (!config.contains("database")) {
            config.set("database", "filesystem");
            config.setComments("database", Collections.singletonList("Supported values: mysql, sqlite, mongodb, filesystem"));
        }

        if (!config.contains("server-name")) {
            config.set("server-name", "Localhost Server");
            config.setComments("server-name", Collections.singletonList("This name will be displayed in the Discord webhook and saved in the database."));
        }
        if (!config.contains("server-address")) {
            config.set("server-address", "localhost");
            config.setComments("server-address", Collections.singletonList("This address will be displayed in the Discord webhook and saved in the database."));
        }
    }
}
