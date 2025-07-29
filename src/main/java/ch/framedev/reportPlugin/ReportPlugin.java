package ch.framedev.reportPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ReportPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        ConfigUtils configUtils = new ConfigUtils(this, getConfig());
        configUtils.initializeConfig(this);

        getCommand("report").setExecutor(new ReportCommand(this));
        getCommand("reportlist").setExecutor(new ReportListCommand(new Database()));
        ReportGUI reportGUI = new ReportGUI(new Database());
        getCommand("reportgui").setExecutor(reportGUI);
        getServer().getPluginManager().registerEvents(reportGUI, this);
        getLogger().info("ReportPlugin has been enabled!");
        getLogger().info("ReportPlugin is running on Server version: " + getServer().getVersion());
    }

    @Override
    public void onDisable() {
        getLogger().info("ReportPlugin has been disabled!");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase("spigottest")) {
            if (!sender.hasPermission("spigottest.use")) {
                sender.sendMessage("You do not have permission to use this command.");
                return true;
            }
            if (args.length == 0) {
                sender.sendMessage("Usage: /spigottest reload");
                return true;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                // Reload logic here
                sender.sendMessage("SpigotTest plugin reloaded successfully!");
                getLogger().info("SpigotTest plugin has been reloaded by " + sender.getName() + "!");
                reloadConfig();
                return true;
            }
        }
        return super.onCommand(sender, command, label, args);
    }
}
