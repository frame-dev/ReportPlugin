package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.*;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReportPlugin extends JavaPlugin {

    private static ReportPlugin instance;
    private Database database;

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        // Initialize the singleton instance
        instance = this;

        // Load the configuration file
        getConfig().options().copyDefaults(true);
        // Save the default config if it doesn't exist
        saveDefaultConfig();

        // Initialize configuration utilities
        ConfigUtils configUtils = new ConfigUtils(getConfig());
        configUtils.initializeConfig(this);

        // Initialize the database connection
        database = new Database(this);

        getCommand("report").setExecutor(new ReportCommand(this, database));
        getCommand("reports-list").setExecutor(new ReportListCommand(database));
        ReportGUI reportGUI = new ReportGUI(database);
        getCommand("report-gui").setExecutor(reportGUI);
        getServer().getPluginManager().registerEvents(reportGUI, this);
        ReportDataCommand reportDataCommand = new ReportDataCommand(database);
        getServer().getPluginManager().registerEvents(reportDataCommand, this);
        getCommand("report-data").setExecutor(reportDataCommand);
        ReportTeleportCommand reportTeleportCommand = new ReportTeleportCommand(database);
        getCommand("reporttp").setExecutor(reportTeleportCommand);
        getCommand("reporttp").setTabCompleter(reportTeleportCommand);

        // Log plugin enable message
        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
        getLogger().info("ReportPlugin is running on Plugin version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        instance = null;
        database = null;
        getLogger().info("ReportPlugin has been disabled!");
    }

    /**
     * Get the singleton instance of the plugin
     * @return the instance of ReportPlugin
     */
    public static ReportPlugin getInstance() {
        return instance;
    }
}
