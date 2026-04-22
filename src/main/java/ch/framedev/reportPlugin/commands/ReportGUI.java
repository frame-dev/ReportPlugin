package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.DiscordUtils;
import ch.framedev.reportPlugin.utils.Report;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ReportGUI implements CommandExecutor, Listener {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static String guiTitle = ChatColor.RED + "Report List";
    private static String updateTitle = ChatColor.BLUE + "Update Report";
    private static String teleportTitle = ChatColor.LIGHT_PURPLE + "Teleport to Location";
    private static String deleteTitle = ChatColor.DARK_RED + "Delete Report";
    private static String kickTitle = ChatColor.DARK_RED + "Kick Player";
    private static String banTitle = ChatColor.DARK_RED + "Ban Player";
    private static String teleportToReporterTitle = ChatColor.LIGHT_PURPLE + "Teleport to Reporter";
    private static String updateHistoryTitle = ChatColor.BLUE + "View Update History";

    private Database database;

    private static class UpdateSession {
        String reportId;
        String newReason;
        String newAdditionalInfo;
        String newResolutionComment;
        Boolean newResolved;
        int step;
    }

    private static class BanSession {
        String reportId;
        String reason;
    }

    private final Map<UUID, UpdateSession> updateSessions = new HashMap<>();
    private final Map<UUID, String> playerSelectedReport = new HashMap<>();
    private final Map<UUID, String> playerKickReason = new HashMap<>();
    private final Map<UUID, BanSession> playerBanSessions = new HashMap<>();
    private final Map<UUID, String> playerViewReport = new HashMap<>();

    public ReportGUI(Database database) {
        this.database = database;
        reloadTitles();
    }

    public void setDatabase(Database database) {
        this.database = database;
        reloadTitles();
    }

    public void clearData() {
        updateSessions.clear();
        playerSelectedReport.clear();
        playerKickReason.clear();
        playerBanSessions.clear();
        playerViewReport.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(message(messages, "messages.only_players", "&cOnly players can use this command."));
            return true;
        }

        if (!player.hasPermission("reportplugin.gui")) {
            player.sendMessage(message(messages, "messages.no_permission", "&cYou do not have permission to use this command."));
            return true;
        }

        if (args.length == 0) {
            openReportList(player);
            return true;
        }

        if (args.length == 1) {
            openSingleReportView(player, args[0], messages);
            return true;
        }

        player.sendMessage(message(messages, "messages.usage_reportgui", "&cUsage: /report-gui [reportId]"));
        return true;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!isTrackedInventory(event)) {
            return;
        }

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || meta.getDisplayName() == null) {
            return;
        }

        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        String displayName = ChatColor.stripColor(meta.getDisplayName());
        UUID uuid = player.getUniqueId();

        if (displayName.startsWith("Report: ")) {
            String reportId = displayName.substring("Report: ".length());
            playerSelectedReport.put(uuid, reportId);
            sendReportDetails(player, reportId, messages);
            return;
        }

        if (displayName.equals(ChatColor.stripColor(updateTitle))) {
            if (!ensureSelectedReport(player, uuid, messages)) {
                return;
            }
            UpdateSession session = new UpdateSession();
            session.reportId = playerSelectedReport.get(uuid);
            updateSessions.put(uuid, session);
            player.closeInventory();
            player.sendMessage(message(messages, "messages.enter_new_report_reason", "&ePlease enter the new reason for the report in chat:"));
            return;
        }

        if (displayName.equals(ChatColor.stripColor(teleportTitle))) {
            if (!ensureSelectedReport(player, uuid, messages)) {
                return;
            }
            Report report = database.getReportById(playerSelectedReport.get(uuid));
            if (report == null) {
                player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
                return;
            }

            World world = Bukkit.getWorld(report.getWorldName());
            if (world == null) {
                String worldNotFound = messages.getString("messages.world_not_found", "&cThe world for this report could not be found. {world}");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', worldNotFound.replace("{world}", report.getWorldName())));
                return;
            }

            player.teleport(Report.getLocationAsBukkitLocation(report.getLocation()));
            player.sendMessage(message(messages, "messages.teleported_to_report_location", "&aTeleported to the reported location."));
            return;
        }

        if (displayName.equals(ChatColor.stripColor(deleteTitle))) {
            if (!ensureSelectedReport(player, uuid, messages)) {
                return;
            }

            String reportId = playerSelectedReport.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
                return;
            }

            if (database.deleteReport(reportId)) {
                String successMessage = messages.getString("messages.report_deleted", "&aReport {reportId} deleted.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', successMessage.replace("{reportId}", reportId)));
            } else {
                String failureMessage = messages.getString("messages.failed_to_delete_report", "&cFailed to delete report {reportId}.");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', failureMessage.replace("{reportId}", reportId)));
            }
            player.closeInventory();
            return;
        }

        if (displayName.equals(ChatColor.stripColor(kickTitle))) {
            if (!prepareModerationAction(player, uuid, playerKickReason, messages, "messages.enter_kick_reason")) {
                return;
            }
            player.closeInventory();
            return;
        }

        if (displayName.equals(ChatColor.stripColor(banTitle))) {
            if (!startBanFlow(player, uuid, messages)) {
                return;
            }
            player.closeInventory();
            return;
        }

        if (displayName.equals(ChatColor.stripColor(teleportToReporterTitle))) {
            if (!ensureSelectedReport(player, uuid, messages)) {
                return;
            }

            Report report = database.getReportById(playerSelectedReport.get(uuid));
            if (report == null) {
                player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
                return;
            }

            Player reporter = Bukkit.getPlayerExact(report.getReporter());
            if (reporter == null || !reporter.isOnline()) {
                player.sendMessage(message(messages, "messages.reporter_not_found", "&cReporter is not online."));
                return;
            }

            player.teleport(reporter.getLocation());
            player.sendMessage(message(messages, "messages.teleported_to_reporter", "&aTeleported to reporter's location."));
            return;
        }

        if (displayName.equals(ChatColor.stripColor(updateHistoryTitle))) {
            if (!ensureSelectedReport(player, uuid, messages)) {
                return;
            }
            showUpdateHistory(player, playerSelectedReport.get(uuid));
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (!updateSessions.containsKey(uuid) && !playerKickReason.containsKey(uuid) && !playerBanSessions.containsKey(uuid)) {
            return;
        }

        event.setCancelled(true);
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        String input = event.getMessage();

        if (playerKickReason.containsKey(uuid)) {
            Bukkit.getScheduler().runTask(ReportPlugin.getInstance(), () -> handleKickReason(player, uuid, input, messages));
            return;
        }

        if (playerBanSessions.containsKey(uuid)) {
            Bukkit.getScheduler().runTask(ReportPlugin.getInstance(), () -> handleBanInput(player, uuid, input, messages));
            return;
        }

        UpdateSession session = updateSessions.get(uuid);
        if (session == null) {
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            updateSessions.remove(uuid);
            Bukkit.getScheduler().runTask(ReportPlugin.getInstance(),
                    () -> player.sendMessage(message(messages, "messages.update_cancelled", "&cUpdate cancelled."))
            );
            return;
        }

        boolean skip = input.equalsIgnoreCase("skip");
        boolean leaveUnchanged = input.equalsIgnoreCase("-");

        switch (session.step) {
            case 0 -> {
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping reason. Type 'skip' to skip any step.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving reason unchanged.");
                } else {
                    session.newReason = input;
                }
                player.sendMessage(ChatColor.GOLD + "Enter the new additional info (or type '-' to leave unchanged):");
                session.step++;
            }
            case 1 -> {
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping additional info.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving additional info unchanged.");
                } else {
                    session.newAdditionalInfo = input;
                }
                player.sendMessage(ChatColor.GOLD + "Enter the new resolution comment (or type '-' to leave unchanged):");
                session.step++;
            }
            case 2 -> {
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping resolution comment.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving resolution comment unchanged.");
                } else {
                    session.newResolutionComment = input;
                }
                player.sendMessage(ChatColor.GOLD + "Is the report resolved? (yes/no):");
                session.step++;
            }
            case 3 -> {
                if (skip || leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving resolved status unchanged.");
                    Bukkit.getScheduler().runTask(ReportPlugin.getInstance(), () -> applyReportUpdate(player, uuid, session));
                    return;
                }

                if (input.equalsIgnoreCase("yes")) {
                    session.newResolved = true;
                } else if (input.equalsIgnoreCase("no")) {
                    session.newResolved = false;
                } else {
                    player.sendMessage(ChatColor.RED + "Please type 'yes' or 'no':");
                    return;
                }

                Bukkit.getScheduler().runTask(ReportPlugin.getInstance(), () -> applyReportUpdate(player, uuid, session));
            }
            default -> updateSessions.remove(uuid);
        }
    }

    private void reloadTitles() {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        guiTitle = message(messages, "gui.titles.report_list", "&cReport List");
        updateTitle = message(messages, "gui.titles.update_report", "&aUpdate Report");
        teleportTitle = message(messages, "gui.titles.teleport_title", "&6Teleport to Reported Location");
        deleteTitle = message(messages, "gui.titles.delete_report", "&4Delete Report");
        kickTitle = message(messages, "gui.titles.kick_player", "&eKick Player");
        banTitle = message(messages, "gui.titles.ban_player", "&cBan Player");
        teleportToReporterTitle = message(messages, "gui.titles.teleport_to_reporter", "&bTeleport to Reporter");
        updateHistoryTitle = message(messages, "gui.titles.view_update_history", "&dView Update History");
    }

    private void openReportList(Player player) {
        List<Report> reports = database.getAllReports().stream()
                .filter(report -> !report.isResolved())
                .toList();

        int minInventorySize = 9;
        int maxInventorySize = 54;
        int actionButtonCount = 7;
        int neededSlots = Math.max(reports.size(), 1) + actionButtonCount;
        int inventorySize = Math.min(Math.max(((neededSlots - 1) / 9 + 1) * 9, minInventorySize), maxInventorySize);

        Inventory gui = Bukkit.createInventory(null, inventorySize, guiTitle);
        int maxReportSlots = inventorySize - actionButtonCount;

        for (int i = 0; i < Math.min(reports.size(), maxReportSlots); i++) {
            gui.setItem(i, createReportItem(reports.get(i), true));
        }

        int firstButtonSlot = inventorySize - actionButtonCount;
        addButton(gui, firstButtonSlot, Material.BOOK, updateHistoryTitle, "View the update history of the selected report");
        addButton(gui, firstButtonSlot + 1, Material.COMPASS, teleportToReporterTitle, "Teleport to the reporter of the selected report");
        addButton(gui, firstButtonSlot + 2, Material.DIAMOND_SWORD, banTitle, "Click to ban the reported player");
        addButton(gui, firstButtonSlot + 3, Material.IRON_BOOTS, kickTitle, "Click to kick the reported player");
        addButton(gui, firstButtonSlot + 4, Material.RED_WOOL, deleteTitle, "Click to delete the selected report");
        addButton(gui, firstButtonSlot + 5, Material.ENDER_PEARL, teleportTitle, "Teleport to the selected report location");
        addButton(gui, firstButtonSlot + 6, Material.WRITABLE_BOOK, updateTitle, "Click to update the selected report");

        player.openInventory(gui);
    }

    private void openSingleReportView(Player player, String reportId, FileConfiguration messages) {
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(ChatColor.RED + "Report with ID " + reportId + " not found.");
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 9, "Report: " + reportId);
        inventory.setItem(0, createReportItem(report, false));
        addButton(inventory, 2, Material.BOOK, updateHistoryTitle, message(messages, "gui.lores.update_history_item", "View the update history of this report"));
        addButton(inventory, 3, Material.COMPASS, teleportToReporterTitle, message(messages, "gui.lores.teleport_to_reporter_item", "Teleport to the reporter's location"));
        addButton(inventory, 4, Material.DIAMOND_SWORD, banTitle, message(messages, "gui.lores.ban_player_item", "Ban the reported player"));
        addButton(inventory, 5, Material.IRON_BOOTS, kickTitle, message(messages, "gui.lores.kick_player_item", "Kick the reported player"));
        addButton(inventory, 6, Material.RED_WOOL, deleteTitle, message(messages, "gui.lores.delete_report_item", "Delete this report"));
        addButton(inventory, 7, Material.ENDER_PEARL, teleportTitle, message(messages, "gui.lores.teleport_to_reported_location_item", "Teleport to the reported location"));
        addButton(inventory, 8, Material.WRITABLE_BOOK, updateTitle, message(messages, "gui.lores.update_report_item", "Click to update this report"));
        player.openInventory(inventory);
        playerViewReport.put(player.getUniqueId(), reportId);
        playerSelectedReport.put(player.getUniqueId(), reportId);
    }

    private ItemStack createReportItem(Report report, boolean includeSelectionHint) {
        ItemStack reportItem = new ItemStack(Material.PAPER);
        ItemMeta meta = reportItem.getItemMeta();
        if (meta == null) {
            return reportItem;
        }

        meta.setDisplayName(ChatColor.YELLOW + "Report: " + report.getReportId());
        List<String> lore = new java.util.ArrayList<>(List.of(
                ChatColor.GRAY + "Player: " + report.getReportedPlayer(),
                ChatColor.GRAY + "Reporter: " + report.getReporter(),
                ChatColor.GRAY + "Reason: " + report.getReason(),
                ChatColor.GRAY + "Time: " + formatTimestamp(report.getTimestamp()),
                ChatColor.GRAY + "Resolved: " + (report.isResolved() ? "Yes" : "No"),
                ChatColor.GRAY + (report.isResolved() ? " (" + report.getResolutionComment() + ")" : ""),
                ChatColor.GRAY + "Server: " + report.getServerName(),
                ChatColor.GRAY + "Location: " + report.getLocation(),
                ChatColor.GRAY + "World: " + report.getWorldName()
        ));
        if (includeSelectionHint) {
            lore.add(ChatColor.GRAY + "Click to select this report");
        }
        meta.setLore(lore);
        reportItem.setItemMeta(meta);
        return reportItem;
    }

    private void addButton(Inventory gui, int slot, Material material, String title, String lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(title);
            meta.setLore(List.of(ChatColor.GRAY + lore));
            item.setItemMeta(meta);
        }
        gui.setItem(slot, item);
    }

    private boolean isTrackedInventory(InventoryClickEvent event) {
        if (event.getView().getTitle().equals(guiTitle)) {
            return true;
        }

        UUID uuid = event.getWhoClicked().getUniqueId();
        String selectedReportId = playerViewReport.get(uuid);
        return selectedReportId != null && event.getView().getTitle().equals("Report: " + selectedReportId);
    }

    private void sendReportDetails(Player player, String reportId, FileConfiguration messages) {
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            return;
        }

        player.sendMessage(ChatColor.GREEN + "---- Report Details ----");
        player.sendMessage(ChatColor.YELLOW + "ID: " + report.getReportId());
        player.sendMessage(ChatColor.YELLOW + "Player: " + report.getReportedPlayer());
        player.sendMessage(ChatColor.YELLOW + "Reporter: " + report.getReporter());
        player.sendMessage(ChatColor.YELLOW + "Reason: " + report.getReason());
        player.sendMessage(ChatColor.YELLOW + "Time: " + formatTimestamp(report.getTimestamp()));
        player.sendMessage(ChatColor.YELLOW + "Server: " + report.getServerName() + " (" + report.getServerIp() + ")");
        player.sendMessage(ChatColor.YELLOW + "World: " + report.getWorldName());
        player.sendMessage(ChatColor.YELLOW + "Location: " + report.getLocation());
        player.sendMessage(ChatColor.YELLOW + "Resolved: " + (report.isResolved() ? "Yes" : "No"));
        if (report.isResolved()) {
            player.sendMessage(ChatColor.YELLOW + "Resolution: " + report.getResolutionComment());
        }
        if (!report.getAdditionalInfo().isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "Additional: " + report.getAdditionalInfo());
        }
        player.sendMessage(message(messages, "messages.update_report_book", "&aTo update this report, click the 'Update Report' book in the GUI."));
    }

    private boolean ensureSelectedReport(Player player, UUID uuid, FileConfiguration messages) {
        if (playerSelectedReport.containsKey(uuid)) {
            return true;
        }

        player.sendMessage(message(messages, "messages.select_first_report", "&cPlease select a report first by clicking on it in the report list."));
        return false;
    }

    private boolean prepareModerationAction(Player player, UUID uuid, Map<UUID, String> targetMap, FileConfiguration messages, String messagePath) {
        if (!ensureSelectedReport(player, uuid, messages)) {
            return false;
        }

        String reportId = playerSelectedReport.get(uuid);
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            return false;
        }

        Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
            player.sendMessage(message(messages, "messages.reported_player_not_found", "&cReported player is not online."));
            return false;
        }

        player.sendMessage(message(messages, messagePath, "&ePlease type the reason in chat:"));
        targetMap.put(uuid, reportId);
        return true;
    }

    private boolean startBanFlow(Player player, UUID uuid, FileConfiguration messages) {
        if (!ensureSelectedReport(player, uuid, messages)) {
            return false;
        }

        String reportId = playerSelectedReport.get(uuid);
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            return false;
        }

        Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
            player.sendMessage(message(messages, "messages.reported_player_not_found", "&cReported player is not online."));
            return false;
        }

        BanSession banSession = new BanSession();
        banSession.reportId = reportId;
        playerBanSessions.put(uuid, banSession);
        player.sendMessage(message(messages, "messages.please_type_ban_reason", "&ePlease type the ban reason in chat:"));
        return true;
    }

    private void showUpdateHistory(Player player, String reportId) {
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(ChatColor.RED + "Report not found.");
            return;
        }

        Map<String, Report> history = database.getUpdateHistory(report);
        if (history.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No update history for this report.");
            return;
        }

        player.sendMessage(ChatColor.GREEN + "---- Update History for Report " + reportId + " ----");
        history.forEach((updater, historyReport) -> {
            String updaterName = updater.replace("_-_", "");
            player.sendMessage(ChatColor.YELLOW + "Updated by: " + updaterName);
            player.sendMessage(ChatColor.YELLOW + "Reason: " + historyReport.getReason());
            player.sendMessage(ChatColor.YELLOW + "Reported Player: " + historyReport.getReportedPlayer());
            player.sendMessage(ChatColor.YELLOW + "Reporter: " + historyReport.getReporter());
            player.sendMessage(ChatColor.YELLOW + "Additional Info: " + (historyReport.getAdditionalInfo().isEmpty() ? "N/A" : historyReport.getAdditionalInfo()));
            player.sendMessage(ChatColor.YELLOW + "Resolved: " + (historyReport.isResolved() ? "Yes" : "No"));
            if (historyReport.isResolved()) {
                player.sendMessage(ChatColor.YELLOW + "Resolution Comment: " + (historyReport.getResolutionComment().isEmpty() ? "N/A" : historyReport.getResolutionComment()));
            }
            player.sendMessage(ChatColor.YELLOW + "Time: " + formatTimestamp(historyReport.getTimestamp()));
            player.sendMessage(ChatColor.GRAY + "-----------------------------");
        });
        player.sendMessage(ChatColor.GREEN + "----------------------------------------");
    }

    private void handleKickReason(Player player, UUID uuid, String reason, FileConfiguration messages) {
        String reportId = playerKickReason.get(uuid);
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            playerKickReason.remove(uuid);
            return;
        }

        Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
            player.sendMessage(message(messages, "messages.reported_player_not_found", "&cReported player is not online."));
            playerKickReason.remove(uuid);
            return;
        }

        reportedPlayer.kickPlayer(ChatColor.RED + "You have been kicked for: " + reason);
        player.sendMessage(ChatColor.GREEN + "Player " + reportedPlayer.getName() + " has been kicked for: " + reason);
        playerKickReason.remove(uuid);
    }

    private void handleBanInput(Player player, UUID uuid, String input, FileConfiguration messages) {
        BanSession banSession = playerBanSessions.get(uuid);
        if (banSession == null) {
            return;
        }

        Report report = database.getReportById(banSession.reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            playerBanSessions.remove(uuid);
            return;
        }

        Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
        if (reportedPlayer == null || !reportedPlayer.isOnline()) {
            player.sendMessage(message(messages, "messages.reported_player_not_found", "&cReported player is not online."));
            playerBanSessions.remove(uuid);
            return;
        }

        if (banSession.reason == null) {
            banSession.reason = input;
            player.sendMessage(message(messages, "messages.enter_ban_duration",
                    "&ePlease enter the ban duration in chat (example: 30m, 12h, 7d, 2w or perm):"));
            return;
        }

        Instant expiresAt;
        try {
            expiresAt = parseBanExpiration(input);
        } catch (IllegalArgumentException exception) {
            player.sendMessage(message(messages, "messages.invalid_ban_duration",
                    "&cInvalid duration. Use values like 30m, 12h, 7d, 2w or perm."));
            return;
        }

        reportedPlayer.ban(banSession.reason, expiresAt, player.getName(), true);
        if (expiresAt == null) {
            player.sendMessage(message(messages, "messages.player_banned_permanently",
                    "&aPlayer {player} has been permanently banned for: {reason}")
                    .replace("{player}", reportedPlayer.getName())
                    .replace("{reason}", banSession.reason));
        } else {
            player.sendMessage(message(messages, "messages.player_banned_temporarily",
                    "&aPlayer {player} has been banned for {duration} because: {reason}")
                    .replace("{player}", reportedPlayer.getName())
                    .replace("{duration}", input)
                    .replace("{reason}", banSession.reason));
        }
        playerBanSessions.remove(uuid);
    }

    private void applyReportUpdate(Player player, UUID uuid, UpdateSession session) {
        Report report = database.getReportById(session.reportId);
        if (report == null) {
            player.sendMessage(ChatColor.RED + "Report not found.");
            updateSessions.remove(uuid);
            return;
        }

        if (session.newReason != null) {
            report.setReason(session.newReason);
        }
        if (session.newAdditionalInfo != null) {
            report.setAdditionalInfo(session.newAdditionalInfo);
        }
        if (session.newResolutionComment != null) {
            report.setResolutionComment(session.newResolutionComment);
        }
        if (session.newResolved != null) {
            report.setResolved(session.newResolved);
        }

        database.updateReport(report);
        updateSessions.remove(uuid);
        player.sendMessage(ChatColor.GREEN + "Report updated!");

        if (database.writeUpdateHistory(report, player.getName())) {
            player.sendMessage(ChatColor.GREEN + "Update history recorded.");
        } else {
            player.sendMessage(ChatColor.RED + "Failed to record update history.");
        }

        notifyStaffAboutUpdate(player, report);
        notifyDiscordAboutUpdate(player, report);
    }

    private void notifyStaffAboutUpdate(Player actor, Report report) {
        boolean notifyUpdate = ReportPlugin.getInstance().getConfig().getBoolean("notify.on-update", false);
        boolean notifyResolved = ReportPlugin.getInstance().getConfig().getBoolean("notify.on-resolve", false);
        boolean shouldNotify = (report.isResolved() && notifyResolved) || (!report.isResolved() && notifyUpdate);

        if (!shouldNotify) {
            return;
        }

        String updateMessage = ChatColor.AQUA + "Report " + report.getReportId() + " has been "
                + (report.isResolved() ? "resolved" : "updated") + " by " + actor.getName() + ".";

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("reportplugin.report.notify")) {
                continue;
            }

            onlinePlayer.sendMessage(updateMessage);
            TextComponent hoverMessage = new TextComponent(ChatColor.GRAY + "Click to view report details");
            hoverMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report-gui " + report.getReportId()));
            onlinePlayer.spigot().sendMessage(hoverMessage);
            playerViewReport.put(onlinePlayer.getUniqueId(), report.getReportId());
        }
    }

    private void notifyDiscordAboutUpdate(Player actor, Report report) {
        if (!ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            return;
        }

        if (report.isResolved()) {
            if (ReportPlugin.getInstance().getConfig().getBoolean("discord.notify.on-resolve", false)
                    && DiscordUtils.sendReportResolvedToDiscord(report)) {
                actor.sendMessage(ChatColor.GREEN + "Discord notified about the resolution.");
            }
            return;
        }

        if (ReportPlugin.getInstance().getConfig().getBoolean("discord.notify.on-update", true)
                && DiscordUtils.sendReportUpdateToDiscord(report)) {
            actor.sendMessage(ChatColor.GREEN + "Discord notified about the update.");
        }
    }

    private String formatTimestamp(long timestamp) {
        synchronized (DATE_FORMAT) {
            return DATE_FORMAT.format(new Date(timestamp));
        }
    }

    private String message(FileConfiguration messages, String path, String defaultValue) {
        return ChatColor.translateAlternateColorCodes('&', messages.getString(path, defaultValue));
    }

    private Instant parseBanExpiration(String input) {
        String normalized = input.trim().toLowerCase(Locale.ROOT).replace(" ", "");
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty.");
        }

        if (normalized.equals("perm") || normalized.equals("perma") || normalized.equals("permanent")) {
            return null;
        }

        Matcher matcher = Pattern.compile("(\\d+)([smhdw])").matcher(normalized);
        Duration totalDuration = Duration.ZERO;
        int processedCharacters = 0;

        while (matcher.find()) {
            if (matcher.start() != processedCharacters) {
                throw new IllegalArgumentException("Invalid duration format.");
            }

            long value = Long.parseLong(matcher.group(1));
            String unit = matcher.group(2);

            totalDuration = switch (unit) {
                case "s" -> totalDuration.plusSeconds(value);
                case "m" -> totalDuration.plusMinutes(value);
                case "h" -> totalDuration.plusHours(value);
                case "d" -> totalDuration.plusDays(value);
                case "w" -> totalDuration.plusDays(value * 7);
                default -> throw new IllegalArgumentException("Invalid duration unit.");
            };

            processedCharacters = matcher.end();
        }

        if (processedCharacters != normalized.length() || totalDuration.isZero() || totalDuration.isNegative()) {
            throw new IllegalArgumentException("Invalid duration format.");
        }

        return Instant.now().plus(totalDuration);
    }
}
