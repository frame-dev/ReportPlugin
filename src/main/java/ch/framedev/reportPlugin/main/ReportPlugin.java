package ch.framedev.reportPlugin.main;

import ch.framedev.reportPlugin.commands.ReportCommand;
import ch.framedev.reportPlugin.commands.ReportDataCommand;
import ch.framedev.reportPlugin.commands.ReportGUI;
import ch.framedev.reportPlugin.commands.ReportListCommand;
import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.ConfigUtils;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReportPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        ConfigUtils configUtils = new ConfigUtils(this, getConfig());
        configUtils.initializeConfig(this);

        Database database = new Database(this);

        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reportlist").setExecutor(new ReportListCommand(database));
        ReportGUI reportGUI = new ReportGUI(database);
        getCommand("reportgui").setExecutor(reportGUI);
        getServer().getPluginManager().registerEvents(reportGUI, this);
        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
        getCommand("reportdata").setExecutor(new ReportDataCommand(database));
    }

    @Override
    public void onDisable() {
        getLogger().info("ReportPlugin has been disabled!");
    }
}
