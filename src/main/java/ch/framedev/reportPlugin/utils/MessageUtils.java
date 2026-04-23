package ch.framedev.reportPlugin.utils;

import ch.framedev.reportPlugin.main.ReportPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public final class MessageUtils {

    private MessageUtils() {
    }

    public static FileConfiguration messages() {
        return ReportPlugin.getInstance().getMessagesConfig();
    }

    public static String get(String path, String defaultValue) {
        return colorize(messages().getString(path, defaultValue));
    }

    public static String format(String path, String defaultValue, String... replacements) {
        String message = get(path, defaultValue);
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            message = message.replace(replacements[index], replacements[index + 1]);
        }
        return message;
    }

    public static List<String> getList(String path, List<String> defaultValue, String... replacements) {
        List<String> configured = messages().getStringList(path);
        List<String> source = configured.isEmpty() ? defaultValue : configured;
        return source.stream()
                .map(MessageUtils::colorize)
                .map(line -> applyReplacements(line, replacements))
                .toList();
    }

    public static void send(CommandSender sender, String path, String defaultValue) {
        sender.sendMessage(get(path, defaultValue));
    }

    public static void send(CommandSender sender, String path, String defaultValue, String... replacements) {
        sender.sendMessage(format(path, defaultValue, replacements));
    }

    private static String applyReplacements(String value, String... replacements) {
        String result = value;
        for (int index = 0; index + 1 < replacements.length; index += 2) {
            result = result.replace(replacements[index], replacements[index + 1]);
        }
        return result;
    }

    private static String colorize(String value) {
        return ChatColor.translateAlternateColorCodes('&', value);
    }
}
