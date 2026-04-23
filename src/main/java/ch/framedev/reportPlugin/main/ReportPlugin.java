package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.*;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import ch.framedev.reportPlugin.utils.MessageUtils;

import java.io.File;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * Main class for the ReportPlugin
 * Handles plugin enable/disable and command registration
 * @version 0.4
 * @author FrameDev
 */
public final class ReportPlugin extends JavaPlugin {

    private static final String MESSAGE_FILE_NAME = "messages.yml";

    /** Singleton instance */
    private static ReportPlugin instance;

    // Database instance
    private Database database;

    private FileConfiguration messagesConfig;

    // Command handlers
    private ReportCommand reportCommand;
    private ReportGUI reportGUI;
    private ReportDataCommand reportDataCommand;
    private ReportTeleportCommand reportTeleportCommand;
    private ReportDeleteCommand reportDeleteCommand;
    private ReportUpdateHistoryCommand reportUpdateHistoryCommand;
    private ReportClearUpdateHistoryCommand reportClearUpdateHistoryCommand;
    private ReportListCommand reportListCommand;
    private ReportHelpCommand reportHelpCommand;

    @Override
    public void onEnable() {
        instance = this;

        initializeConfiguration();

        if (!initializeDatabase()) {
            getLogger().severe("Failed to connect to the database. Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.reportCommand = new ReportCommand(this, database);
        this.reportListCommand = new ReportListCommand(database);
        this.reportGUI = new ReportGUI(database);
        this.reportDataCommand = new ReportDataCommand(database);
        this.reportTeleportCommand = new ReportTeleportCommand(database);
        this.reportDeleteCommand = new ReportDeleteCommand(database);
        this.reportUpdateHistoryCommand = new ReportUpdateHistoryCommand(database);
        this.reportClearUpdateHistoryCommand = new ReportClearUpdateHistoryCommand(database);
        this.reportHelpCommand = new ReportHelpCommand();
        registerCommandsAndEvents();

        // Log plugin enable message
        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
        getLogger().info("ReportPlugin is running on Plugin version: " + getDescription().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("Disabling ReportPlugin...");
        cleanupRuntimeState();
        getLogger().info("ReportPlugin has been disabled!");
        instance = null;
    }

    /**
     * Get the singleton instance of the plugin
     * @return the instance of ReportPlugin
     */
    public static ReportPlugin getInstance() {
        return instance;
    }

    public Database getDatabase() {
        return database;
    }

    private void reloadPlugin() {
        reloadConfig();
        initializeConfiguration();
        disconnectDatabase();

        database = new Database(this);
        if (!database.connect()) {
            getLogger().severe("Failed to reconnect to the database after reload.");
            return;
        }

        getLogger().info("Database connection re-established successfully!");
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

    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }

    private void initializeConfiguration() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        new ConfigUtils(getConfig()).initializeConfig(this);
        setupMessageFile();
        reloadMessagesConfig();
        getLogger().info("Configuration loaded successfully!");
    }

    private boolean initializeDatabase() {
        getLogger().info("Setting up the database connection...");
        database = new Database(this);
        if (!database.connect()) {
            return false;
        }

        getLogger().info("Database connection established successfully!");
        return true;
    }

    private void registerCommandsAndEvents() {
        getLogger().info("Registering commands and events...");
        registerCommand("report", reportCommand, null);
        registerCommand("reports-list", reportListCommand, null);
        registerCommand("report-gui", reportGUI, null);
        registerCommand("report-data", reportDataCommand, null);
        registerCommand("report-help", reportHelpCommand, null);
        registerCommand("reporttp", reportTeleportCommand, reportTeleportCommand);
        registerCommand("report-delete", reportDeleteCommand, reportDeleteCommand);
        registerCommand("report-updatehistory", reportUpdateHistoryCommand, reportUpdateHistoryCommand);
        registerCommand("report-clearupdatehistory", reportClearUpdateHistoryCommand, reportClearUpdateHistoryCommand);

        registerListener(reportGUI);
        registerListener(reportDataCommand);
        getLogger().info("Commands and events registered successfully!");
    }

    private void registerCommand(String commandName, org.bukkit.command.CommandExecutor executor, TabCompleter tabCompleter) {
        PluginCommand pluginCommand = getCommand(commandName);
        if (pluginCommand == null) {
            throw new IllegalStateException("Command is missing from plugin.yml: " + commandName);
        }

        pluginCommand.setExecutor(executor);
        if (tabCompleter != null) {
            pluginCommand.setTabCompleter(tabCompleter);
        }
    }

    private void registerListener(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private void cleanupRuntimeState() {
        if (reportGUI != null) {
            reportGUI.clearData();
            getLogger().info("Cleared GUI data.");
        }

        disconnectDatabase();
        database = null;
        messagesConfig = null;
        reportCommand = null;
        reportGUI = null;
        reportDataCommand = null;
        reportTeleportCommand = null;
        reportDeleteCommand = null;
        reportUpdateHistoryCommand = null;
        reportClearUpdateHistoryCommand = null;
        reportListCommand = null;
        reportHelpCommand = null;
    }

    private void disconnectDatabase() {
        if (database == null) {
            return;
        }

        database.disconnect();
        getLogger().info("Database connection closed.");
    }

    private void reloadMessagesConfig() {
        File messageFile = new File(getDataFolder(), MESSAGE_FILE_NAME);
        messagesConfig = YamlConfiguration.loadConfiguration(messageFile);
    }

    private void setupMessageFile() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        File messageFile = new File(getDataFolder(), MESSAGE_FILE_NAME);
        if (!messageFile.exists()) {
            saveResource(MESSAGE_FILE_NAME, false);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(command.getName().equalsIgnoreCase("reportplugin")) {
            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("reportplugin.reload")) {
                    reloadPlugin();
                    MessageUtils.send(sender, "messages.reportplugin_reload_success",
                            "&aReportPlugin configuration reloaded successfully.");
                } else {
                    MessageUtils.send(sender, "messages.reportplugin_reload_no_permission",
                            "&cYou do not have permission to execute this command.");
                }
            } else {
                MessageUtils.send(sender, "messages.usage_reportplugin_reload",
                        "&cUsage: /{label} reload",
                        "{label}", label);
            }
            return true;
        }
        return super.onCommand(sender, command, label, args);
    }
}
