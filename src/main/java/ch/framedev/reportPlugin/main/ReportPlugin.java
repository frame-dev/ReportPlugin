package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.ReportCommand;
import ch.framedev.reportPlugin.commands.ReportDataCommand;
import ch.framedev.reportPlugin.commands.ReportGUI;
import ch.framedev.reportPlugin.commands.ReportListCommand;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReportPlugin extends JavaPlugin {

    private static ReportPlugin instance;
    private Database database;

    @Override
    public void onEnable() {
        instance = this;

        // Load the configuration file
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();

        // Initialize configuration utilities
        ConfigUtils configUtils = new ConfigUtils(getConfig());
        configUtils.initializeConfig(this);

        // Initialize the database connection
        database = new Database(this);

        getCommand("report").setExecutor(new ReportCommand(this, database));
        getCommand("reportlist").setExecutor(new ReportListCommand(database));
        ReportGUI reportGUI = new ReportGUI(database);
        getCommand("reportgui").setExecutor(reportGUI);
        getServer().getPluginManager().registerEvents(reportGUI, this);
        getCommand("reportdata").setExecutor(new ReportDataCommand(database));

        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("ReportPlugin has been disabled!");
        instance = null;
        database = null;
    }

    public static ReportPlugin getInstance() {
        return instance;
    }
}
