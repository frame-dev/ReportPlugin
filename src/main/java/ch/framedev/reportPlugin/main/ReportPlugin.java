package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.*;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for the ReportPlugin
 * Handles plugin enable/disable and command registration
 * @version 0.4
 * @author FrameDev
 */
public final class ReportPlugin extends JavaPlugin {

    /** Singleton instance */
    private static ReportPlugin instance;

    // Database instance
    private Database database;

    // Command handlers
    private ReportCommand reportCommand;
    private ReportGUI reportGUI;
    private ReportDataCommand reportDataCommand;
    private ReportTeleportCommand reportTeleportCommand;
    private ReportDeleteCommand reportDeleteCommand;
    private ReportUpdateHistoryCommand reportUpdateHistoryCommand;
    private ReportClearUpdateHistoryCommand reportClearUpdateHistoryCommand;
    private ReportListCommand reportListCommand;

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
        this.reportCommand = new ReportCommand(this, database);
        getCommand("report").setExecutor(reportCommand);
        getLogger().info("Report command registered.");
        this.reportListCommand = new ReportListCommand(database);
        getCommand("reports-list").setExecutor(reportListCommand);
        getLogger().info("Reports list command registered.");
        this.reportGUI = new ReportGUI(database);
        getCommand("report-gui").setExecutor(reportGUI);
        getLogger().info("Report GUI command registered.");
        getServer().getPluginManager().registerEvents(reportGUI, this);
        this.reportDataCommand = new ReportDataCommand(database);
        getServer().getPluginManager().registerEvents(reportDataCommand, this);
        getCommand("report-data").setExecutor(reportDataCommand);
        getLogger().info("Report data command registered.");
        this.reportTeleportCommand = new ReportTeleportCommand(database);
        getCommand("reporttp").setExecutor(reportTeleportCommand);
        getCommand("reporttp").setTabCompleter(reportTeleportCommand);
        getLogger().info("Report teleport command registered.");
        this.reportDeleteCommand = new ReportDeleteCommand(database);
        getCommand("report-delete").setExecutor(reportDeleteCommand);
        getCommand("report-delete").setTabCompleter(reportDeleteCommand);
        getLogger().info("Report delete command registered.");
        this.reportUpdateHistoryCommand = new ReportUpdateHistoryCommand(database);
        getCommand("report-updatehistory").setExecutor(reportUpdateHistoryCommand);
        getCommand("report-updatehistory").setTabCompleter(reportUpdateHistoryCommand);
        getLogger().info("Report update history command registered.");
        this.reportClearUpdateHistoryCommand = new ReportClearUpdateHistoryCommand(database);
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
        getLogger().info("Cleared GUI data.");
        // Nullify references
        this.reportCommand = null;
        this.reportGUI = null;
        this.reportDataCommand = null;
        this.reportTeleportCommand = null;
        this.reportDeleteCommand = null;
        this.reportUpdateHistoryCommand = null;
        this.reportClearUpdateHistoryCommand = null;
        this.reportListCommand = null;
        getLogger().info("Cleared command handler references.");
        // Close database connection
        if (database != null) {
            database.disconnect();
            getLogger().info("Database connection closed.");
        }
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

    private void reloadPlugin() {
        reloadConfig();
        getLogger().info("Configuration reloaded!");
        ConfigUtils configUtils = new ConfigUtils(getConfig());
        configUtils.initializeConfig(this);
        getLogger().info("Configuration applied successfully!");
        database = new Database(this);
        if (database.connect()) {
            getLogger().info("Database connection re-established successfully!");
        } else {
            getLogger().severe("Failed to reconnect to the database after reload.");
        }
        reportCommand.setDatabase(database);
        reportGUI.setDatabase(database);
        reportDataCommand.setDatabase(database);
        reportTeleportCommand.setDatabase(database);
        reportDeleteCommand.setDatabase(database);
        reportUpdateHistoryCommand.setDatabase(database);
        reportClearUpdateHistoryCommand.setDatabase(database);
        reportListCommand.setDatabase(database);
        getLogger().info("Database instance updated in all command handlers.");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("reportplugin")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("reportplugin.reload")) {
                    reloadPlugin();
                    sender.sendMessage("§aReportPlugin configuration reloaded successfully.");
                } else {
                    sender.sendMessage("§cYou do not have permission to execute this command.");
                }
            } else {
                sender.sendMessage("§cUsage: /" + label + " reload");
            }
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }
}
