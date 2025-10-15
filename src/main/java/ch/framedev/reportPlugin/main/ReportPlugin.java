package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.*;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Main class for the ReportPlugin
 * Handles plugin enable/disable and command registration
 * @version 0.4
 * @author FrameDev
 */
public final class ReportPlugin extends JavaPlugin {

    /** Singleton instance */
    private static ReportPlugin instance;
    private Database database;

    private ReportGUI reportGUI;

    @SuppressWarnings("DataFlowIssue")
    @Override
    public void onEnable() {
        // Initialize the singleton instance
        instance = this;

        // Load the configuration file
        getConfig().options().copyDefaults(true);
        // Save the default config if it doesn't exist
        saveDefaultConfig();
        getLogger().info("Default configuration file created!");

        // Initialize configuration utilities
        getLogger().info("Loading configuration...");
        ConfigUtils configUtils = new ConfigUtils(getConfig());
        configUtils.initializeConfig(this);
        getLogger().info("Configuration loaded successfully!");

        // Initialize the database connection
        getLogger().info("Setting up the database connection...");
        database = new Database(this);
        if (database.connect()) {
            getLogger().info("Database connection established successfully!");
        } else {
            getLogger().severe("Failed to connect to the database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Registering commands and events...");
        getCommand("report").setExecutor(new ReportCommand(this, database));
        getLogger().info("Report command registered.");
        getCommand("reports-list").setExecutor(new ReportListCommand(database));
        getLogger().info("Reports list command registered.");
        this.reportGUI = new ReportGUI(database);
        getCommand("report-gui").setExecutor(reportGUI);
        getLogger().info("Report GUI command registered.");
        getServer().getPluginManager().registerEvents(reportGUI, this);
        ReportDataCommand reportDataCommand = new ReportDataCommand(database);
        getServer().getPluginManager().registerEvents(reportDataCommand, this);
        getCommand("report-data").setExecutor(reportDataCommand);
        getLogger().info("Report data command registered.");
        ReportTeleportCommand reportTeleportCommand = new ReportTeleportCommand(database);
        getCommand("reporttp").setExecutor(reportTeleportCommand);
        getCommand("reporttp").setTabCompleter(reportTeleportCommand);
        getLogger().info("Report teleport command registered.");
        ReportDeleteCommand reportDeleteCommand = new ReportDeleteCommand(database);
        getCommand("report-delete").setExecutor(reportDeleteCommand);
        getCommand("report-delete").setTabCompleter(reportDeleteCommand);
        getLogger().info("Report delete command registered.");
        ReportUpdateHistoryCommand reportUpdateHistoryCommand = new ReportUpdateHistoryCommand(database);
        getCommand("report-updatehistory").setExecutor(reportUpdateHistoryCommand);
        getCommand("report-updatehistory").setTabCompleter(reportUpdateHistoryCommand);
        getLogger().info("Report update history command registered.");
        ReportClearUpdateHistoryCommand reportClearUpdateHistoryCommand = new ReportClearUpdateHistoryCommand(database);
        getCommand("report-clearupdatehistory").setExecutor(reportClearUpdateHistoryCommand);
        getCommand("report-clearupdatehistory").setTabCompleter(reportClearUpdateHistoryCommand);
        getLogger().info("Report clear update history command registered.");
        getLogger().info("Commands and events registered successfully!");

        // Log plugin enable message
        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
        getLogger().info("ReportPlugin is running on Plugin version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ReportPlugin...");
        instance = null;
        // Clear Data
        reportGUI.clearData();
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
