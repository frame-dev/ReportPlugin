package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ReportTeleportCommand implements CommandExecutor, TabCompleter {

    private Database database;

    public ReportTeleportCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();

        if (!(sender instanceof Player player)) {
            sender.sendMessage(message(messages, "messages.only_players", "&cThis command can only be executed by players."));
            return true;
        }

        if (!player.hasPermission("reportplugin.reporttp")) {
            player.sendMessage(message(messages, "messages.no_permission", "&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length < 1 || args.length > 2) {
            player.sendMessage(message(messages, "messages.usage_reporttp", "&cUsage: /reporttp <reportedPlayer|reportId> [reportId]"));
            return true;
        }

        if (args.length == 1) {
            String lookup = args[0];
            Report report = database.getReportById(lookup);
            if (report != null) {
                teleportToReport(player, report, messages, false);
                return true;
            }

            Report lastReport = getLatestReportForPlayer(lookup);
            if (lastReport == null) {
                player.sendMessage(message(messages, "messages.reporttp_report_or_player_not_found",
                        "&cNo report found for player or report ID: {input}")
                        .replace("{input}", lookup));
                return true;
            }

            teleportToReport(player, lastReport, messages, true);
            return true;
        }

        if (args.length == 2) {
            String reportedPlayerName = args[0];
            String reportId = args[1];
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(message(messages, "messages.reporttp_report_id_not_found",
                        "&cNo report found with ID: {reportId}")
                        .replace("{reportId}", reportId));
                return true;
            }

            if (report.getReportedPlayer() == null || !report.getReportedPlayer().equalsIgnoreCase(reportedPlayerName)) {
                player.sendMessage(message(messages, "messages.reporttp_report_player_mismatch",
                        "&cReport {reportId} belongs to {actualPlayer}, not {expectedPlayer}.")
                        .replace("{reportId}", reportId)
                        .replace("{actualPlayer}", report.getReportedPlayer() == null ? "unknown" : report.getReportedPlayer())
                        .replace("{expectedPlayer}", reportedPlayerName));
                return true;
            }

            teleportToReport(player, report, messages, false);
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("reporttp")) {
            return null;
        }

        if (args.length == 1) {
            String prefix = args[0].toLowerCase(Locale.ROOT);
            Set<String> suggestions = new LinkedHashSet<>();

            database.getAllReports().stream()
                    .map(Report::getReportedPlayer)
                    .filter(name -> name != null && name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .forEach(suggestions::add);

            database.getAllReports().stream()
                    .map(Report::getReportId)
                    .filter(reportId -> reportId != null && reportId.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .forEach(suggestions::add);

            return suggestions.stream().toList();
        }

        if (args.length == 2) {
            String reportedPlayerName = args[0];
            return database.getAllReports().stream()
                    .filter(report -> report.getReportedPlayer() != null && report.getReportedPlayer().equalsIgnoreCase(reportedPlayerName))
                    .map(Report::getReportId)
                    .filter(reportId -> reportId != null && reportId.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .toList();
        }

        return List.of();
    }

    private Report getLatestReportForPlayer(String reportedPlayerName) {
        return database.getAllReports().stream()
                .filter(report -> report.getReportedPlayer() != null
                        && report.getReportedPlayer().equalsIgnoreCase(reportedPlayerName))
                .max(Comparator.comparingLong(Report::getTimestamp))
                .orElse(null);
    }

    private void teleportToReport(Player player, Report report, FileConfiguration messages, boolean latestLookup) {
        Location reportLocation = getValidReportLocation(report);
        if (reportLocation == null) {
            String worldName = report.getWorldName() == null || report.getWorldName().isBlank() ? "unknown" : report.getWorldName();
            player.sendMessage(message(messages, "messages.world_not_found", "&cThe world for this report could not be found. {world}")
                    .replace("{world}", worldName));
            return;
        }

        if (!reportLocation.getChunk().isLoaded()) {
            reportLocation.getChunk().load();
        }

        boolean teleported = player.teleport(reportLocation);
        if (!teleported) {
            player.sendMessage(message(messages, "messages.reporttp_failed_teleport",
                    "&cTeleport failed for report {reportId}.")
                    .replace("{reportId}", report.getReportId()));
            return;
        }

        String responsePath = latestLookup
                ? "messages.reporttp_latest_report_teleport_success"
                : "messages.reporttp_teleport_success";
        String defaultMessage = latestLookup
                ? "&aTeleported to the latest report for {player} ({reportId}) in world {world}."
                : "&aTeleported to report {reportId} for {player} in world {world}.";

        player.sendMessage(message(messages, responsePath, defaultMessage)
                .replace("{reportId}", report.getReportId())
                .replace("{player}", report.getReportedPlayer() == null ? "unknown" : report.getReportedPlayer())
                .replace("{world}", report.getWorldName() == null || report.getWorldName().isBlank() ? "unknown" : report.getWorldName()));
    }

    private Location getValidReportLocation(Report report) {
        try {
            if (report == null || report.getLocation() == null || report.getLocation().isBlank()) {
                return null;
            }

            Location reportLocation = Report.getLocationAsBukkitLocation(report.getLocation());
            World world = reportLocation.getWorld();
            if (world == null) {
                return null;
            }

            return reportLocation;
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String message(FileConfiguration messages, String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(path, defaultValue));
    }
}
