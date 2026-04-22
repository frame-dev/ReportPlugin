package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReportHelpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();

        if (!sender.hasPermission("reportplugin.help")) {
            sender.sendMessage(colorize(messages.getString("messages.no_permission", "&cYou do not have permission to use this command.")));
            return true;
        }

        List<String> helpLines = messages.getStringList("help.lines");
        if (helpLines.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No help messages are configured.");
            return true;
        }

        for (String line : helpLines) {
            sender.sendMessage(colorize(line));
        }
        return true;
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
