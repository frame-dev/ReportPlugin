package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.DiscordUtils;
import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.utils.ReportStatus;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
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
import java.util.ArrayList;
import java.util.Comparator;
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
    private static String singleReportTitlePrefix = "Report: ";

    private static String guiTitle = "";
    private static String updateTitle = "";
    private static String teleportTitle = "";
    private static String deleteTitle = "";
    private static String kickTitle = "";
    private static String banTitle = "";
    private static String teleportToReporterTitle = "";
    private static String updateHistoryTitle = "";
    private static String sortTitle = "";
    private static String filterTitle = "";

    private Database database;

    private enum GuiSortMode {
        NEWEST("Newest"),
        OLDEST("Oldest"),
        REPORTED_PLAYER("Reported Player"),
        REPORTER("Reporter"),
        STATUS("Status");

        private final String displayName;

        GuiSortMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public GuiSortMode next() {
            GuiSortMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private enum GuiFilterMode {
        ACTIVE_ONLY("Active Only"),
        CLOSED_ONLY("Closed Only"),
        ALL("All Reports");

        private final String displayName;

        GuiFilterMode(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public GuiFilterMode next() {
            GuiFilterMode[] values = values();
            return values[(ordinal() + 1) % values.length];
        }
    }

    private static final class GuiViewSettings {
        private GuiSortMode sortMode = GuiSortMode.NEWEST;
        private GuiFilterMode filterMode = GuiFilterMode.ACTIVE_ONLY;
    }

    private static final class UpdateSession {
        private String reportId;
        private String newReason;
        private String newStaffNotes;
        private String newEvidenceUrl;
        private String newResolutionComment;
        private ReportStatus newStatus;
        private int step;
    }

    private static final class BanSession {
        private String reportId;
        private String reason;
    }

    private final Map<UUID, UpdateSession> updateSessions = new HashMap<>();
    private final Map<UUID, String> playerSelectedReport = new HashMap<>();
    private final Map<UUID, String> playerKickReason = new HashMap<>();
    private final Map<UUID, BanSession> playerBanSessions = new HashMap<>();
    private final Map<UUID, GuiViewSettings> guiViewSettings = new HashMap<>();

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
        guiViewSettings.clear();
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
        if (!isTrackedInventory(event.getView().getTitle())) {
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

        if (displayName.startsWith(singleReportTitlePrefix)) {
            String reportId = displayName.substring(singleReportTitlePrefix.length());
            playerSelectedReport.put(uuid, reportId);
            sendReportDetails(player, reportId, messages);
            return;
        }

        if (displayName.equals(ChatColor.stripColor(filterTitle))) {
            GuiViewSettings settings = getGuiViewSettings(uuid);
            settings.filterMode = settings.filterMode.next();
            openReportList(player);
            return;
        }

        if (displayName.equals(ChatColor.stripColor(sortTitle))) {
            GuiViewSettings settings = getGuiViewSettings(uuid);
            settings.sortMode = settings.sortMode.next();
            openReportList(player);
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
            player.sendMessage(message(messages, "messages.update_flow_help",
                    "&7Type 'skip' or '-' to keep a value. Type 'clear' to remove notes, evidence, or resolution comment."));
            player.sendMessage(message(messages, "messages.enter_new_report_reason",
                    "&ePlease enter the new reason for the report in chat:"));
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

            Location reportLocation = safeLocation(report);
            World world = reportLocation == null ? null : reportLocation.getWorld();
            if (world == null) {
                String worldNotFound = messages.getString("messages.world_not_found", "&cThe world for this report could not be found. {world}");
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', worldNotFound.replace("{world}", report.getWorldName())));
                return;
            }

            player.teleport(reportLocation);
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

        Bukkit.getScheduler().runTask(ReportPlugin.getInstance(), () -> handleUpdateInput(player, uuid, input, messages));
    }

    private void reloadTitles() {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        guiTitle = message(messages, "gui.titles.report_list", "&cReport List");
        singleReportTitlePrefix = message(messages, "gui.titles.single_report_prefix", "Report: ");
        updateTitle = message(messages, "gui.titles.update_report", "&aUpdate Report");
        teleportTitle = message(messages, "gui.titles.teleport_title", "&6Teleport to Reported Location");
        deleteTitle = message(messages, "gui.titles.delete_report", "&4Delete Report");
        kickTitle = message(messages, "gui.titles.kick_player", "&eKick Player");
        banTitle = message(messages, "gui.titles.ban_player", "&cBan Player");
        teleportToReporterTitle = message(messages, "gui.titles.teleport_to_reporter", "&bTeleport to Reporter");
        updateHistoryTitle = message(messages, "gui.titles.view_update_history", "&dView Update History");
        sortTitle = message(messages, "gui.titles.sort_reports", "&6Sort Reports");
        filterTitle = message(messages, "gui.titles.filter_reports", "&bFilter Reports");
    }

    private void openReportList(Player player) {
        GuiViewSettings settings = getGuiViewSettings(player.getUniqueId());
        List<Report> reports = getFilteredAndSortedReports(settings);

        int actionButtonCount = 9;
        int displayedReports = Math.min(reports.size(), 45);
        int inventorySize = Math.min(Math.max(((displayedReports + actionButtonCount - 1) / 9 + 1) * 9, 9), 54);
        Inventory gui = Bukkit.createInventory(null, inventorySize, guiTitle);

        int maxReportSlots = inventorySize - actionButtonCount;
        for (int i = 0; i < Math.min(reports.size(), maxReportSlots); i++) {
            gui.setItem(i, createReportItem(reports.get(i), true, ReportPlugin.getInstance().getMessagesConfig()));
        }

        int firstButtonSlot = inventorySize - actionButtonCount;
        addButton(gui, firstButtonSlot, Material.HOPPER, filterTitle,
                message(ReportPlugin.getInstance().getMessagesConfig(), "gui.lores.filter_reports_item", "Current filter: {value}. Click to cycle.")
                        .replace("{value}", settings.filterMode.getDisplayName()));
        addButton(gui, firstButtonSlot + 1, Material.CLOCK, sortTitle,
                message(ReportPlugin.getInstance().getMessagesConfig(), "gui.lores.sort_reports_item", "Current sort: {value}. Click to cycle.")
                        .replace("{value}", settings.sortMode.getDisplayName()));
        addButton(gui, firstButtonSlot + 2, Material.BOOK, updateHistoryTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.update_history_item", "View the update history of the selected report"));
        addButton(gui, firstButtonSlot + 3, Material.COMPASS, teleportToReporterTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.teleport_to_reporter_item", "Teleport to the reporter of the selected report"));
        addButton(gui, firstButtonSlot + 4, Material.DIAMOND_SWORD, banTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.ban_player_item", "Click to ban the reported player"));
        addButton(gui, firstButtonSlot + 5, Material.IRON_BOOTS, kickTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.kick_player_item", "Click to kick the reported player"));
        addButton(gui, firstButtonSlot + 6, Material.RED_WOOL, deleteTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.delete_report_item", "Click to delete the selected report"));
        addButton(gui, firstButtonSlot + 7, Material.ENDER_PEARL, teleportTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.teleport_to_reported_location_item", "Teleport to the selected report location"));
        addButton(gui, firstButtonSlot + 8, Material.WRITABLE_BOOK, updateTitle, message(ReportPlugin.getInstance().getMessagesConfig(),
                "gui.lores.update_report_item", "Click to update this report"));

        player.openInventory(gui);
    }

    private List<Report> getFilteredAndSortedReports(GuiViewSettings settings) {
        Comparator<Report> comparator = switch (settings.sortMode) {
            case NEWEST -> Comparator.comparingLong(Report::getTimestamp).reversed();
            case OLDEST -> Comparator.comparingLong(Report::getTimestamp);
            case REPORTED_PLAYER -> Comparator.comparing(report -> report.getReportedPlayer().toLowerCase(Locale.ROOT));
            case REPORTER -> Comparator.comparing(report -> report.getReporter().toLowerCase(Locale.ROOT));
            case STATUS -> Comparator.comparing(report -> report.getStatus().getDisplayName());
        };

        return database.getAllReports().stream()
                .filter(report -> switch (settings.filterMode) {
                    case ACTIVE_ONLY -> !report.getStatus().isClosed();
                    case CLOSED_ONLY -> report.getStatus().isClosed();
                    case ALL -> true;
                })
                .sorted(comparator.thenComparing(Comparator.comparingLong(Report::getTimestamp).reversed()))
                .toList();
    }

    private void openSingleReportView(Player player, String reportId, FileConfiguration messages) {
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_gui_not_found", "&cReport with ID {reportId} not found.")
                    .replace("{reportId}", reportId));
            return;
        }

        Inventory inventory = Bukkit.createInventory(null, 9, singleReportTitlePrefix + reportId);
        inventory.setItem(0, createReportItem(report, false, messages));
        addButton(inventory, 2, Material.BOOK, updateHistoryTitle, message(messages, "gui.lores.update_history_item", "View the update history of this report"));
        addButton(inventory, 3, Material.COMPASS, teleportToReporterTitle, message(messages, "gui.lores.teleport_to_reporter_item", "Teleport to the reporter's location"));
        addButton(inventory, 4, Material.DIAMOND_SWORD, banTitle, message(messages, "gui.lores.ban_player_item", "Ban the reported player"));
        addButton(inventory, 5, Material.IRON_BOOTS, kickTitle, message(messages, "gui.lores.kick_player_item", "Kick the reported player"));
        addButton(inventory, 6, Material.RED_WOOL, deleteTitle, message(messages, "gui.lores.delete_report_item", "Delete this report"));
        addButton(inventory, 7, Material.ENDER_PEARL, teleportTitle, message(messages, "gui.lores.teleport_to_reported_location_item", "Teleport to the reported location"));
        addButton(inventory, 8, Material.WRITABLE_BOOK, updateTitle, message(messages, "gui.lores.update_report_item", "Click to update this report"));
        player.openInventory(inventory);
        playerSelectedReport.put(player.getUniqueId(), reportId);
    }

    private ItemStack createReportItem(Report report, boolean includeSelectionHint, FileConfiguration messages) {
        ItemStack reportItem = new ItemStack(Material.PAPER);
        ItemMeta meta = reportItem.getItemMeta();
        if (meta == null) {
            return reportItem;
        }

        meta.setDisplayName(ChatColor.YELLOW + singleReportTitlePrefix + report.getReportId());
        List<String> lore = new ArrayList<>(List.of(
                message(messages, "gui.report_item.player", "&7Player: {player}").replace("{player}", report.getReportedPlayer()),
                message(messages, "gui.report_item.reporter", "&7Reporter: {reporter}").replace("{reporter}", report.getReporter()),
                message(messages, "gui.report_item.reason", "&7Reason: {reason}").replace("{reason}", report.getReason()),
                message(messages, "gui.report_item.time", "&7Time: {time}").replace("{time}", formatTimestamp(report.getTimestamp())),
                message(messages, "gui.report_item.status", "&7Status: {status}").replace("{status}", report.getStatus().getDisplayName()),
                message(messages, "gui.report_item.server", "&7Server: {server}").replace("{server}", report.getServerName()),
                message(messages, "gui.report_item.location", "&7Location: {location}").replace("{location}", report.getLocation()),
                message(messages, "gui.report_item.world", "&7World: {world}").replace("{world}", report.getWorldName())
        ));
        if (!report.getAdditionalInfo().isEmpty()) {
            lore.add(message(messages, "gui.report_item.notes", "&7Notes: {notes}").replace("{notes}", report.getAdditionalInfo()));
        }
        if (!report.getEvidenceUrl().isEmpty()) {
            lore.add(message(messages, "gui.report_item.evidence", "&7Evidence: {evidence}").replace("{evidence}", report.getEvidenceUrl()));
        }
        if (report.getStatus().isClosed() && !report.getResolutionComment().isEmpty()) {
            lore.add(message(messages, "gui.report_item.resolution", "&7Resolution: {resolution}")
                    .replace("{resolution}", report.getResolutionComment()));
        }
        if (includeSelectionHint) {
            lore.add(message(messages, "gui.report_item.selection_hint", "&7Click to select this report"));
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

    private boolean isTrackedInventory(String title) {
        return title.equals(guiTitle) || title.startsWith(singleReportTitlePrefix);
    }

    private void sendReportDetails(Player player, String reportId, FileConfiguration messages) {
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            return;
        }

        player.sendMessage(message(messages, "messages.report_details_header", "&a---- Report Details ----"));
        player.sendMessage(message(messages, "messages.report_details_id", "&eID: {reportId}")
                .replace("{reportId}", report.getReportId()));
        player.sendMessage(message(messages, "messages.report_details_player", "&ePlayer: {player}")
                .replace("{player}", report.getReportedPlayer()));
        player.sendMessage(message(messages, "messages.report_details_reporter", "&eReporter: {reporter}")
                .replace("{reporter}", report.getReporter()));
        player.sendMessage(message(messages, "messages.report_details_reason", "&eReason: {reason}")
                .replace("{reason}", report.getReason()));
        player.sendMessage(message(messages, "messages.report_details_time", "&eTime: {time}")
                .replace("{time}", formatTimestamp(report.getTimestamp())));
        player.sendMessage(message(messages, "messages.report_details_status", "&eStatus: {status}")
                .replace("{status}", report.getStatus().getDisplayName()));
        player.sendMessage(message(messages, "messages.report_details_server", "&eServer: {server} ({ip})")
                .replace("{server}", report.getServerName())
                .replace("{ip}", report.getServerIp()));
        player.sendMessage(message(messages, "messages.report_details_world", "&eWorld: {world}")
                .replace("{world}", report.getWorldName()));
        player.sendMessage(message(messages, "messages.report_details_location", "&eLocation: {location}")
                .replace("{location}", report.getLocation()));
        player.sendMessage(message(messages, "messages.report_details_staff_notes", "&eStaff Notes: {notes}")
                .replace("{notes}", report.getAdditionalInfo().isEmpty() ? "N/A" : report.getAdditionalInfo()));
        player.sendMessage(message(messages, "messages.report_details_evidence", "&eEvidence: {evidence}")
                .replace("{evidence}", report.getEvidenceUrl().isEmpty() ? "N/A" : report.getEvidenceUrl()));
        if (report.getStatus().isClosed()) {
            player.sendMessage(message(messages, "messages.report_details_resolution", "&eResolution: {resolution}")
                    .replace("{resolution}", report.getResolutionComment().isEmpty() ? "N/A" : report.getResolutionComment()));
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

    private void handleUpdateInput(Player player, UUID uuid, String input, FileConfiguration messages) {
        UpdateSession session = updateSessions.get(uuid);
        if (session == null) {
            return;
        }

        if (input.equalsIgnoreCase("cancel")) {
            updateSessions.remove(uuid);
            player.sendMessage(message(messages, "messages.update_cancelled", "&cUpdate cancelled."));
            return;
        }

        boolean skip = input.equalsIgnoreCase("skip") || input.equals("-");
        boolean clear = input.equalsIgnoreCase("clear");

        switch (session.step) {
            case 0 -> {
                if (!skip) {
                    session.newReason = clear ? "" : input;
                }
                player.sendMessage(message(messages, "messages.enter_staff_notes",
                        "&eEnter staff notes (type 'skip' or '-' to keep, 'clear' to remove):"));
                session.step++;
            }
            case 1 -> {
                if (!skip) {
                    session.newStaffNotes = clear ? "" : input;
                }
                player.sendMessage(message(messages, "messages.enter_evidence_url",
                        "&eEnter an evidence URL (type 'skip' or '-' to keep, 'clear' to remove):"));
                session.step++;
            }
            case 2 -> {
                if (!skip) {
                    session.newEvidenceUrl = clear ? "" : input;
                }
                player.sendMessage(message(messages, "messages.enter_resolution_comment",
                        "&eEnter the resolution comment (type 'skip' or '-' to keep, 'clear' to remove):"));
                session.step++;
            }
            case 3 -> {
                if (!skip) {
                    session.newResolutionComment = clear ? "" : input;
                }
                player.sendMessage(message(messages, "messages.enter_report_status",
                        "&eEnter the new status: open, in_progress, resolved, rejected, punished"));
                session.step++;
            }
            case 4 -> {
                if (!skip) {
                    ReportStatus parsedStatus = ReportStatus.parseUserInput(input);
                    if (parsedStatus == null) {
                        player.sendMessage(message(messages, "messages.invalid_report_status",
                                "&cInvalid status. Use open, in_progress, resolved, rejected, or punished."));
                        return;
                    }
                    session.newStatus = parsedStatus;
                }
                applyReportUpdate(player, uuid, session);
            }
            default -> updateSessions.remove(uuid);
        }
    }

    private void showUpdateHistory(Player player, String reportId) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        Report report = database.getReportById(reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            return;
        }

        Map<String, Report> history = database.getUpdateHistory(report);
        if (history.isEmpty()) {
            player.sendMessage(message(messages, "messages.update_history_empty", "&eNo update history for this report."));
            return;
        }

        player.sendMessage(message(messages, "messages.update_history_header", "&a---- Update History for Report {reportId} ----")
                .replace("{reportId}", reportId));
        history.forEach((updater, historyReport) -> {
            String updaterName = updater.replace("_-_", "");
            player.sendMessage(message(messages, "messages.update_history_updated_by", "&eUpdated by: {updater}")
                    .replace("{updater}", updaterName));
            player.sendMessage(message(messages, "messages.update_history_reason", "&eReason: {reason}")
                    .replace("{reason}", historyReport.getReason()));
            player.sendMessage(message(messages, "messages.update_history_reported_player", "&eReported Player: {player}")
                    .replace("{player}", historyReport.getReportedPlayer()));
            player.sendMessage(message(messages, "messages.update_history_reporter", "&eReporter: {reporter}")
                    .replace("{reporter}", historyReport.getReporter()));
            player.sendMessage(message(messages, "messages.update_history_status", "&eStatus: {status}")
                    .replace("{status}", historyReport.getStatus().getDisplayName()));
            player.sendMessage(message(messages, "messages.update_history_staff_notes", "&eStaff Notes: {notes}")
                    .replace("{notes}", historyReport.getAdditionalInfo().isEmpty() ? "N/A" : historyReport.getAdditionalInfo()));
            player.sendMessage(message(messages, "messages.update_history_evidence", "&eEvidence: {evidence}")
                    .replace("{evidence}", historyReport.getEvidenceUrl().isEmpty() ? "N/A" : historyReport.getEvidenceUrl()));
            if (historyReport.getStatus().isClosed()) {
                player.sendMessage(message(messages, "messages.update_history_resolution", "&eResolution Comment: {resolution}")
                        .replace("{resolution}", historyReport.getResolutionComment().isEmpty() ? "N/A" : historyReport.getResolutionComment()));
            }
            player.sendMessage(message(messages, "messages.update_history_time", "&eTime: {time}")
                    .replace("{time}", formatTimestamp(historyReport.getTimestamp())));
            player.sendMessage(message(messages, "messages.update_history_separator", "&7-----------------------------"));
        });
        player.sendMessage(message(messages, "messages.update_history_footer", "&a----------------------------------------"));
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

        reportedPlayer.kickPlayer(message(messages, "messages.kicked_player_message", "&cYou have been kicked for: {reason}")
                .replace("{reason}", reason));
        player.sendMessage(message(messages, "messages.kick_success", "&aPlayer {player} has been kicked for: {reason}")
                .replace("{player}", reportedPlayer.getName())
                .replace("{reason}", reason));
        playerKickReason.remove(uuid);
        markReportAsPunished(report, player, "Kicked: " + reason);
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
            markReportAsPunished(report, player, "Permanently banned: " + banSession.reason);
        } else {
            player.sendMessage(message(messages, "messages.player_banned_temporarily",
                    "&aPlayer {player} has been banned for {duration} because: {reason}")
                    .replace("{player}", reportedPlayer.getName())
                    .replace("{duration}", input)
                    .replace("{reason}", banSession.reason));
            markReportAsPunished(report, player, "Temporarily banned for " + input + ": " + banSession.reason);
        }
        playerBanSessions.remove(uuid);
    }

    private void applyReportUpdate(Player player, UUID uuid, UpdateSession session) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        Report report = database.getReportById(session.reportId);
        if (report == null) {
            player.sendMessage(message(messages, "messages.report_not_found", "&cReport not found."));
            updateSessions.remove(uuid);
            return;
        }

        if (session.newReason != null) {
            report.setReason(session.newReason);
        }
        if (session.newStaffNotes != null) {
            report.setAdditionalInfo(session.newStaffNotes);
        }
        if (session.newEvidenceUrl != null) {
            report.setEvidenceUrl(session.newEvidenceUrl);
        }
        if (session.newResolutionComment != null) {
            report.setResolutionComment(session.newResolutionComment);
        }
        if (session.newStatus != null) {
            report.setStatus(session.newStatus);
        }

        database.updateReport(report);
        updateSessions.remove(uuid);
        player.sendMessage(message(messages, "messages.report_updated", "&aReport updated."));

        if (database.writeUpdateHistory(report, player.getName())) {
            player.sendMessage(message(messages, "messages.update_history_recorded", "&aUpdate history recorded."));
        } else {
            player.sendMessage(message(messages, "messages.update_history_record_failed", "&cFailed to record update history."));
        }

        notifyStaffAboutUpdate(player, report);
        notifyDiscordAboutUpdate(player, report);
    }

    private void markReportAsPunished(Report report, Player actor, String resolutionComment) {
        report.setStatus(ReportStatus.PUNISHED);
        report.setResolutionComment(resolutionComment);
        database.updateReport(report);
        database.writeUpdateHistory(report, actor.getName());
        notifyStaffAboutUpdate(actor, report);
        notifyDiscordAboutUpdate(actor, report);
    }

    private void notifyStaffAboutUpdate(Player actor, Report report) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        boolean notifyUpdate = ReportPlugin.getInstance().getConfig().getBoolean("notify.on-update", false);
        boolean notifyResolved = ReportPlugin.getInstance().getConfig().getBoolean("notify.on-resolve", false);
        boolean shouldNotify = report.getStatus().isClosed() ? notifyResolved : notifyUpdate;

        if (!shouldNotify) {
            return;
        }

        String updateMessage = message(messages, "messages.staff_report_update_notification",
                "&bReport {reportId} is now {status} by {actor}.")
                .replace("{reportId}", report.getReportId())
                .replace("{status}", report.getStatus().getDisplayName())
                .replace("{actor}", actor.getName());

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (!onlinePlayer.hasPermission("reportplugin.report.notify")) {
                continue;
            }

            onlinePlayer.sendMessage(updateMessage);
            TextComponent hoverMessage = new TextComponent(message(messages, "messages.staff_click_view_report",
                    "&7Click to view report details"));
            hoverMessage.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/report-gui " + report.getReportId()));
            onlinePlayer.spigot().sendMessage(hoverMessage);
        }
    }

    private void notifyDiscordAboutUpdate(Player actor, Report report) {
        FileConfiguration messages = ReportPlugin.getInstance().getMessagesConfig();
        if (!ReportPlugin.getInstance().getConfig().getBoolean("useDiscordWebhook", false)) {
            return;
        }

        if (report.getStatus().isClosed()) {
            if (ReportPlugin.getInstance().getConfig().getBoolean("discord.notify.on-resolve", false)
                    && DiscordUtils.sendReportResolvedToDiscord(report)) {
                actor.sendMessage(message(messages, "messages.discord_notified_status_change",
                        "&aDiscord notified about the status change."));
            }
            return;
        }

        if (ReportPlugin.getInstance().getConfig().getBoolean("discord.notify.on-update", true)
                && DiscordUtils.sendReportUpdateToDiscord(report)) {
            actor.sendMessage(message(messages, "messages.discord_notified_update",
                    "&aDiscord notified about the update."));
        }
    }

    private GuiViewSettings getGuiViewSettings(UUID uuid) {
        return guiViewSettings.computeIfAbsent(uuid, ignored -> new GuiViewSettings());
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

    private Location safeLocation(Report report) {
        try {
            return Report.getLocationAsBukkitLocation(report.getLocation());
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
