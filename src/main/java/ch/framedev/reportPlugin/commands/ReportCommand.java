package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.UUID;

import static ch.framedev.reportPlugin.utils.DiscordUtils.sendReportToDiscord;

public class ReportCommand implements CommandExecutor {

    private final ReportPlugin plugin;
    private Database database;

    public ReportCommand(ReportPlugin plugin, Database database) {
        this.plugin = plugin;
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + label + " <player> [reason]");
            return true;
        }
        if (!sender.hasPermission("reportplugin.report")) {
            sender.sendMessage("§cYou do not have permission to report players.");
            return true;
        }
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players.");
            return true;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            sender.sendMessage("§cYou cannot report yourself.");
            return true;
        }

        String reportedPlayer = args[0];
        int maxReportsPerPlayer = plugin.getConfig().getInt("report-settings.max-reports-per-player", 3);
        int maxReportsPerReporter = plugin.getConfig().getInt("report-settings.max-reports-per-reporter", 10);
        long reportsAgainstPlayer = database.getAllReports().stream()
                .filter(report -> report.getReportedPlayer() != null)
                .filter(report -> report.getReportedPlayer().equalsIgnoreCase(reportedPlayer))
                .count();
        long reportsByReporter = database.getAllReports().stream()
                .filter(report -> report.getReporter() != null)
                .filter(report -> report.getReporter().equalsIgnoreCase(player.getName()))
                .count();

        if (maxReportsPerPlayer > 0 && reportsAgainstPlayer >= maxReportsPerPlayer) {
            sender.sendMessage("§cThis player has reached the configured report limit.");
            return true;
        }

        if (maxReportsPerReporter > 0 && reportsByReporter >= maxReportsPerReporter) {
            sender.sendMessage("§cYou have reached the configured report limit.");
            return true;
        }

        long duplicateWindowSeconds = plugin.getConfig().getLong("report-settings.duplicate-window-seconds", 300L);
        if (duplicateWindowSeconds > 0 && isDuplicateReport(player.getName(), reportedPlayer, duplicateWindowSeconds)) {
            FileConfiguration messages = plugin.getMessagesConfig();
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                    messages.getString("messages.duplicate_report_recently",
                            "&cYou already reported this player recently. Please wait before sending the same report again.")));
            return true;
        }

        report(args, player);
        return true;
    }

    private boolean isDuplicateReport(String reporter, String reportedPlayer, long duplicateWindowSeconds) {
        long now = System.currentTimeMillis();
        long duplicateWindowMillis = duplicateWindowSeconds * 1000L;
        return database.getAllReports().stream()
                .filter(report -> report.getReporter() != null && report.getReportedPlayer() != null)
                .anyMatch(report -> report.getReporter().equalsIgnoreCase(reporter)
                        && report.getReportedPlayer().equalsIgnoreCase(reportedPlayer)
                        && now - report.getTimestamp() < duplicateWindowMillis);
    }

    private void report(String[] args, Player player) {
        String reportedPlayer = args[0];
        String reason = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : "No reason provided";
        player.sendMessage("§aYou reported §e" + reportedPlayer + "§a for: §f" + reason);

        Report report = new Report(
                reportedPlayer,
                reason,
                player.getName(),
                UUID.randomUUID().toString(),
                plugin.getConfig().getString("server-name", "Localhost"),
                plugin.getConfig().getString("server-address", "localhost"),
                Bukkit.getVersion(),
                player.getWorld().getName(),
                Report.getLocationAsString(player.getLocation())
        );
        database.insertReport(report);
        Bukkit.getLogger().info("Report created by " + player.getName() + ": " + report.getReportedPlayer() + " for reason: " + report.getReason());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.hasPermission("reportplugin.report.notify")) {
                boolean enabledNotify = plugin.getConfig().getBoolean("notify.on-create", true);
                if (enabledNotify) {
                    onlinePlayer.sendMessage("§cNew report: §e" + player.getName() + " §areported §e" + reportedPlayer + " §afor: §f" + reason + ".");
                    boolean isHoverEnabled = plugin.getConfig().getBoolean("notify.hoverable-teleport", true);
                    if (isHoverEnabled) {
                        TextComponent textComponent = new TextComponent("§7[§eClick to Teleport to Report Location§7]");
                        Location location = Report.getLocationAsBukkitLocation(report.getLocation());
                        textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/tp " + location.getX() + " " + location.getY() + " " + location.getZ()));
                        onlinePlayer.spigot().sendMessage(textComponent);
                    }
                }
            }
        }

        if (plugin.getConfig().getBoolean("useDiscordWebhook", false)
                && plugin.getConfig().getBoolean("discord.notify.on-create", true)) {
            if (!sendReportToDiscord(plugin, report)) {
                Bukkit.getLogger().severe("Failed to send report to Discord.");
            } else {
                Bukkit.getLogger().info("Report sent to Discord successfully.");
            }
        }
    }
}
