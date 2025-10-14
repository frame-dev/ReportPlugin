package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.*;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReportPlugin extends JavaPlugin {

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
        getCommand("reports-list").setExecutor(new ReportListCommand(database));
        this.reportGUI = new ReportGUI(database);
        getCommand("report-gui").setExecutor(reportGUI);
        getServer().getPluginManager().registerEvents(reportGUI, this);
        ReportDataCommand reportDataCommand = new ReportDataCommand(database);
        getServer().getPluginManager().registerEvents(reportDataCommand, this);
        getCommand("report-data").setExecutor(reportDataCommand);
        ReportTeleportCommand reportTeleportCommand = new ReportTeleportCommand(database);
        getCommand("reporttp").setExecutor(reportTeleportCommand);
        getCommand("reporttp").setTabCompleter(reportTeleportCommand);
        ReportDeleteCommand reportDeleteCommand = new ReportDeleteCommand(database);
        getCommand("report-delete").setExecutor(reportDeleteCommand);
        getCommand("report-delete").setTabCompleter(reportDeleteCommand);
        ReportUpdateHistoryCommand reportUpdateHistoryCommand = new ReportUpdateHistoryCommand(database);
        getCommand("report-updatehistory").setExecutor(reportUpdateHistoryCommand);
        getCommand("report-updatehistory").setTabCompleter(reportUpdateHistoryCommand);
        ReportClearUpdateHistoryCommand reportClearUpdateHistoryCommand = new ReportClearUpdateHistoryCommand(database);
        getCommand("report-clearupdatehistory").setExecutor(reportClearUpdateHistoryCommand);
        getCommand("report-clearupdatehistory").setTabCompleter(reportClearUpdateHistoryCommand);
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
