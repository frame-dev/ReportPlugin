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

public class ConfigUtils {

    private final FileConfiguration config;

    public ConfigUtils(ReportPlugin plugin, FileConfiguration config) {
        this.config = config;
    }

    public void initializeConfig(ReportPlugin plugin) {
        setupConfig(plugin);
        plugin.saveConfig();
    }

    private void setupConfig(ReportPlugin plugin) {
        if(!config.contains("discord")) {
            ConfigurationSection section = config.createSection("discord");
            section.set("webhook-url", "YOUR_WEBHOOK_URL_HERE");
            section.set("username", "SpigotTest Bot");
            section.set("avatar-url", "https://example.com/avatar.png");
            section.set("content", "This is a test message from SpigotTest!");
            section.set("embed.title", "Test Embed");
            section.set("embed.description", "This is a description of the test embed.");
            section.set("embed.color", 16777215); // White color
            section.set("embed.url", "https://example.com");
            section.set("embed.footer.text", "Footer text");
            section.set("embed.footer.icon-url", "https://example.com/footer-icon.png");
            section.set("embed.image.url", "https://example.com/image.png");
            section.set("embed.thumbnail.url", "https://example.com/thumbnail.png");
            config.set("discord", section);
        }
    }
}
