// src/main/java/ch/framedev/spigotTest/ReportGUI.java
package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.main.ReportPlugin;
import ch.framedev.reportPlugin.utils.DiscordWebhook;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGUI implements CommandExecutor, Listener {

    private static final String GUI_TITLE = ChatColor.RED + "Report List";
    private static final String UPDATE_TITLE = ChatColor.BLUE + "Update Report";
    private static final String TELEPORT_TITLE = ChatColor.LIGHT_PURPLE + "Teleport to Location";
    private static final String DELETE_TITLE = ChatColor.DARK_RED + "Delete Report";
    private static final String KICK_TITLE = ChatColor.DARK_RED + "Kick Player";
    private static final String BAN_TITLE = ChatColor.DARK_RED + "Ban Player";
    private static final String TELEPORT_TO_REPORTER = ChatColor.LIGHT_PURPLE + "Teleport to Reporter";
    private static final String UPDATE_HISTORY_TITLE = ChatColor.BLUE + "View Update History";

    private final Database database;

    // Track update state per player
    private static class UpdateSession {
        String reportId;
        String newReason;
        String newAdditionalInfo;
        String newResolutionComment;
        Boolean newResolved;
        int step = 0;
    }

    private final Map<UUID, UpdateSession> updateSessions = new HashMap<>();
    private final Map<UUID, String> playerSelectedReport = new HashMap<>();
    private final Map<UUID, String> playerKickReason = new HashMap<>();
    private final Map<UUID, String> playerBanReason = new HashMap<>();

    public ReportGUI(Database database) {
        this.database = database;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("reportplugin.gui")) {
            player.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        List<Report> reports = database.getAllReports();
        int reportCount = reports.size();
        int size = ((reportCount - 1) / 9 + 1) * 9;
        size = Math.max(9, Math.min(size, 54));

        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE);

        for (int i = 0; i < Math.min(reports.size(), size - 7); i++) {
            Report report = reports.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Report: " + report.getReportId());
                meta.setLore(List.of(
                        ChatColor.GRAY + "Player: " + report.getReportedPlayer(),
                        ChatColor.GRAY + "Reporter: " + report.getReporter(),
                        ChatColor.GRAY + "Reason: " + report.getReason(),
                        ChatColor.GRAY + "Time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(report.getTimestamp())),
                        ChatColor.GRAY + "Resolved: " + (report.isResolved() ? "Yes" : "No"),
                        ChatColor.GRAY + (report.isResolved() ? " (" + report.getResolutionComment() + ")" : ""),
                        ChatColor.GRAY + "Server: " + report.getServerName(),
                        ChatColor.GRAY + "Location: " + report.getLocation(),
                        ChatColor.GRAY + "World: " + report.getWorldName(),
                        ChatColor.GRAY + "Click to select this report"
                ));
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

        // View Update History button
        ItemStack historyItem = new ItemStack(Material.BOOK);
        ItemMeta historyMeta = historyItem.getItemMeta();
        if (historyMeta != null) {
            historyMeta.setDisplayName(UPDATE_HISTORY_TITLE);
            historyMeta.setLore(List.of(ChatColor.GRAY + "View the update history of the selected report"));
            historyItem.setItemMeta(historyMeta);
        }
        gui.setItem(size - 7, historyItem);

        // Teleport to Reporter button
        ItemStack teleportToReporterItem = new ItemStack(Material.COMPASS);
        ItemMeta teleportToReporterMeta = teleportToReporterItem.getItemMeta();
        if (teleportToReporterMeta != null) {
            teleportToReporterMeta.setDisplayName(TELEPORT_TO_REPORTER);
            teleportToReporterMeta.setLore(List.of(ChatColor.GRAY + "Teleport to the reporter of the selected report"));
            teleportToReporterItem.setItemMeta(teleportToReporterMeta);
        }
        gui.setItem(size - 6, teleportToReporterItem);

        // Ban button
        ItemStack banItem = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta banMeta = banItem.getItemMeta();
        if (banMeta != null) {
            banMeta.setDisplayName(BAN_TITLE);
            banMeta.setLore(List.of(ChatColor.GRAY + "Click to ban the reported player"));
            banItem.setItemMeta(banMeta);
        }
        gui.setItem(size - 5, banItem);

        // Kick button
        ItemStack kickItem = new ItemStack(Material.IRON_BOOTS);
        ItemMeta kickMeta = kickItem.getItemMeta();
        if (kickMeta != null) {
            kickMeta.setDisplayName(KICK_TITLE);
            kickMeta.setLore(List.of(ChatColor.GRAY + "Click to kick the reported player"));
            kickItem.setItemMeta(kickMeta);
        }
        gui.setItem(size - 4, kickItem);

        // Delete button
        ItemStack deleteItem = new ItemStack(Material.RED_WOOL);
        ItemMeta deleteMeta = deleteItem.getItemMeta();
        if (deleteMeta != null) {
            deleteMeta.setDisplayName(DELETE_TITLE);
            deleteMeta.setLore(List.of(ChatColor.GRAY + "Click to delete the selected report"));
            deleteItem.setItemMeta(deleteMeta);
        }
        gui.setItem(size - 3, deleteItem);

        // Teleport button
        ItemStack teleportItem = new ItemStack(Material.ENDER_PEARL);
        ItemMeta teleportMeta = teleportItem.getItemMeta();
        if (teleportMeta != null) {
            teleportMeta.setDisplayName(TELEPORT_TITLE);
            teleportMeta.setLore(List.of(ChatColor.GRAY + "Teleport to the selected report location"));
            teleportItem.setItemMeta(teleportMeta);
        }
        gui.setItem(size - 2, teleportItem);

        // Update button
        ItemStack updateItem = new ItemStack(Material.WRITABLE_BOOK);
        ItemMeta updateMeta = updateItem.getItemMeta();
        if (updateMeta != null) {
            updateMeta.setDisplayName(UPDATE_TITLE);
            updateMeta.setLore(List.of(ChatColor.GRAY + "Click to update the selected report"));
            updateItem.setItemMeta(updateMeta);
        }
        gui.setItem(size - 1, updateItem);

        player.openInventory(gui);
        return true;
    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals(GUI_TITLE)) return;
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;
        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        } else {
            meta.getDisplayName();
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        UUID uuid = player.getUniqueId();

        if (displayName.startsWith("Report: ")) {
            String reportId = displayName.substring("Report: ".length());
            playerSelectedReport.put(uuid, reportId);
            Report report = database.getReportById(reportId);
            if (report != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                player.sendMessage(ChatColor.GREEN + "---- Report Details ----");
                player.sendMessage(ChatColor.YELLOW + "ID: " + report.getReportId());
                player.sendMessage(ChatColor.YELLOW + "Player: " + report.getReportedPlayer());
                player.sendMessage(ChatColor.YELLOW + "Reporter: " + report.getReporter());
                player.sendMessage(ChatColor.YELLOW + "Reason: " + report.getReason());
                player.sendMessage(ChatColor.YELLOW + "Time: " + sdf.format(new Date(report.getTimestamp())));
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
                player.sendMessage(ChatColor.AQUA + "To update this report, click the 'Update Report' book in the GUI.");
            }
        } else if (displayName.equals(ChatColor.stripColor(UPDATE_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            UpdateSession session = new UpdateSession();
            session.reportId = playerSelectedReport.get(uuid);
            updateSessions.put(uuid, session);
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Enter the new reason for the report:");
        } else if (displayName.equals(ChatColor.stripColor(TELEPORT_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            Report report = database.getReportById(playerSelectedReport.get(uuid));
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                return;
            }
            World world = Bukkit.getWorld(report.getWorldName());
            if (world == null) {
                player.sendMessage(ChatColor.RED + "World not found: " + report.getWorldName());
                return;
            }
            player.teleport(Report.getLocationAsBukkitLocation(report.getLocation()));
            player.sendMessage(ChatColor.GREEN + "Teleported to report location.");
        } else if (displayName.equals(ChatColor.stripColor(DELETE_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            String reportId = playerSelectedReport.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                return;
            }
            if(database.deleteReport(reportId)) {
                player.sendMessage(ChatColor.GREEN + "Report " + reportId + " deleted.");
            } else {
                player.sendMessage(ChatColor.RED + "Failed to delete report " + reportId + ".");
            }
            player.sendMessage(ChatColor.GREEN + "Report " + reportId + " deleted.");
            player.closeInventory();
        } else if(displayName.equals(ChatColor.stripColor(KICK_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            String reportId = playerSelectedReport.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                return;
            }
            Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
            if (reportedPlayer == null || !reportedPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "Reported player is not online.");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Please type the kick reason in chat:");
            playerKickReason.put(uuid, reportId);
            player.closeInventory();
        } else if(displayName.equals(ChatColor.stripColor(BAN_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            String reportId = playerSelectedReport.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                return;
            }
            Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
            if (reportedPlayer == null || !reportedPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "Reported player is not online.");
                return;
            }
            player.sendMessage(ChatColor.GREEN + "Please type the ban reason in chat:");
            playerBanReason.put(uuid, reportId);
            player.closeInventory();
        } else if(displayName.equals(ChatColor.stripColor(TELEPORT_TO_REPORTER))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            Report report = database.getReportById(playerSelectedReport.get(uuid));
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                return;
            }
            Player reporter = Bukkit.getPlayerExact(report.getReporter());
            if (reporter == null || !reporter.isOnline()) {
                player.sendMessage(ChatColor.RED + "Reporter is not online.");
                return;
            }
            player.teleport(reporter.getLocation());
            player.sendMessage(ChatColor.GREEN + "Teleported to reporter's location.");
        } else if(displayName.equals(ChatColor.stripColor(UPDATE_HISTORY_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            String reportId = playerSelectedReport.get(uuid);
            Map<String, Report> history = database.getUpdateHistory(database.getReportById(reportId));
            if (history.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "No update history for this report.");
            } else {
                player.sendMessage(ChatColor.GREEN + "---- Update History for Report " + reportId + " ----");
                history.forEach((updater, rep) -> {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    player.sendMessage(ChatColor.YELLOW + "Updated by: " + updater);
                    player.sendMessage(ChatColor.YELLOW + "Reason: " + rep.getReason());
                    player.sendMessage(ChatColor.YELLOW + "Additional Info: " + (rep.getAdditionalInfo().isEmpty() ? "N/A" : rep.getAdditionalInfo()));
                    player.sendMessage(ChatColor.YELLOW + "Resolved: " + (rep.isResolved() ? "Yes" : "No"));
                    if (rep.isResolved()) {
                        player.sendMessage(ChatColor.YELLOW + "Resolution Comment: " + (rep.getResolutionComment().isEmpty() ? "N/A" : rep.getResolutionComment()));
                    }
                    player.sendMessage(ChatColor.YELLOW + "Time: " + sdf.format(new Date(rep.getTimestamp())));
                    player.sendMessage(ChatColor.GRAY + "-----------------------------");
                });
                player.sendMessage(ChatColor.GREEN + "----------------------------------------");
            }
        }
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        UUID uuid = player.getUniqueId();
        if (!updateSessions.containsKey(uuid)) return;
        if(playerKickReason.containsKey(uuid)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            String reportId = playerKickReason.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                playerKickReason.remove(uuid);
                return;
            }
            Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
            if (reportedPlayer == null || !reportedPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "Reported player is not online.");
                playerKickReason.remove(uuid);
                return;
            }
            reportedPlayer.kickPlayer(ChatColor.RED + "You have been kicked for: " + reason);
            player.sendMessage(ChatColor.GREEN + "Player " + reportedPlayer.getName() + " has been kicked for: " + reason);
            playerKickReason.remove(uuid);
            return;
        }
        if(playerBanReason.containsKey(uuid)) {
            event.setCancelled(true);
            String reason = event.getMessage();
            String reportId = playerBanReason.get(uuid);
            Report report = database.getReportById(reportId);
            if (report == null) {
                player.sendMessage(ChatColor.RED + "Report not found.");
                playerBanReason.remove(uuid);
                return;
            }
            Player reportedPlayer = Bukkit.getPlayerExact(report.getReportedPlayer());
            if (reportedPlayer == null || !reportedPlayer.isOnline()) {
                player.sendMessage(ChatColor.RED + "Reported player is not online.");
                playerBanReason.remove(uuid);
                return;
            }
            Bukkit.getBanList(BanList.Type.NAME).addBan(reportedPlayer.getName(), reason, null, player.getName());
            reportedPlayer.kickPlayer(ChatColor.RED + "You have been banned for: " + reason);
            player.sendMessage(ChatColor.GREEN + "Player " + reportedPlayer.getName() + " has been banned for: " + reason);
            playerBanReason.remove(uuid);
            return;
        }

        event.setCancelled(true);
        UpdateSession session = updateSessions.get(uuid);
        String input = event.getMessage();

        if (input.equalsIgnoreCase("cancel")) {
            player.sendMessage(ChatColor.RED + "Update cancelled.");
            updateSessions.remove(uuid);
            return;
        }

        boolean skip = input.equalsIgnoreCase("skip");
        boolean leaveUnchanged = input.equalsIgnoreCase("-");

        switch (session.step) {
            case 0:
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping reason. Type 'skip' to skip any step.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving reason unchanged.");
                } else {
                    session.newReason = input;
                }
                player.sendMessage(ChatColor.GOLD + "Enter the new additional info (or type '-' to leave unchanged):");
                session.step++;
                break;
            case 1:
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping additional info.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving additional info unchanged.");
                } else {
                    session.newAdditionalInfo = input;
                }
                player.sendMessage(ChatColor.GOLD + "Enter the new resolution comment (or type '-' to leave unchanged):");
                session.step++;
                break;
            case 2:
                if (skip) {
                    player.sendMessage(ChatColor.YELLOW + "Skipping resolution comment.");
                } else if (leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving resolution comment unchanged.");
                } else {
                    session.newResolutionComment = input;
                }
                player.sendMessage(ChatColor.GOLD + "Is the report resolved? (yes/no):");
                session.step++;
                break;
            case 3:
                if (skip || leaveUnchanged) {
                    player.sendMessage(ChatColor.YELLOW + "Leaving resolved status unchanged.");
                    // Apply updates
                    Report report = database.getReportById(session.reportId);
                    if (report == null) {
                        player.sendMessage(ChatColor.RED + "Report not found.");
                        updateSessions.remove(uuid);
                        return;
                    }
                    if (session.newReason != null) report.setReason(session.newReason);
                    if (session.newAdditionalInfo != null) report.setAdditionalInfo(session.newAdditionalInfo);
                    if (session.newResolutionComment != null) report.setResolutionComment(session.newResolutionComment);
                    // Do not change resolved status
                    database.updateReport(report);
                    player.sendMessage(ChatColor.GREEN + "Report updated!");
                    updateSessions.remove(uuid);
                    break;
                }
                if (input.equalsIgnoreCase("yes")) {
                    session.newResolved = true;
                } else if (input.equalsIgnoreCase("no")) {
                    session.newResolved = false;
                } else {
                    player.sendMessage(ChatColor.RED + "Please type 'yes' or 'no':");
                    return;
                }
                // Apply updates
                Report report = database.getReportById(session.reportId);
                if (report == null) {
                    player.sendMessage(ChatColor.RED + "Report not found.");
                    updateSessions.remove(uuid);
                    return;
                }
                if (session.newReason != null) report.setReason(session.newReason);
                if (session.newAdditionalInfo != null) report.setAdditionalInfo(session.newAdditionalInfo);
                if (session.newResolutionComment != null) report.setResolutionComment(session.newResolutionComment);
                report.setResolved(session.newResolved);
                database.updateReport(report);
                player.sendMessage(ChatColor.GREEN + "Report updated!");
                updateSessions.remove(uuid);
                database.writeUpdateHistory(report, player.getName());
                sendDiscordWebhookUpdate(report);
                break;
        }
    }

    private void sendDiscordWebhookUpdate(Report report) {
        // Implement Discord webhook update logic here
        FileConfiguration config = ReportPlugin.getInstance().getConfig();
        if (!config.getBoolean("useDiscordWebhook", false)) return;
        String webhookUrl = config.getString("discord.update.webhook-url");
        if (webhookUrl == null || webhookUrl.isEmpty()) return;
        DiscordWebhook discordWebhook = new DiscordWebhook(webhookUrl);
        discordWebhook.setUsername(config.getString("discord.update.username", "ReportBot"));
        discordWebhook.setAvatarUrl(config.getString("discord.update.avatar-url", "https://example.com/avatar.png"));
        discordWebhook.setContent(config.getString("discord.update.content", "Report updated!"));
        DiscordWebhook.EmbedObject embed = new DiscordWebhook.EmbedObject();
        embed.setTitle(config.getString("discord.update.embed.title", "Report Updated"));
        String description = config.getString("discord.update.embed.description", "");
        description = description.replace("%ReportedPlayer%", report.getReportedPlayer())
                .replace("%Reporter%", report.getReporter())
                .replace("%Reason%", report.getReason())
                .replace("%AdditionalInfo%", report.getAdditionalInfo() != null ? report.getAdditionalInfo() : "N/A")
                .replace("%Status%", report.isResolved() ? "Resolved" : "Unresolved")
                .replace("%ResolutionComment%", report.getResolutionComment() != null ? report.getResolutionComment() : "N/A")
                .replace("%ServerName%", report.getServerName())
                .replace("%Location%", report.getLocation())
                .replace("%WorldName%", report.getWorldName());
        embed.setDescription(description);
        embed.setUrl(config.getString("discord.update.embed.url", "https://example.com"));
        embed.setColor(java.awt.Color.BLUE);
        embed.setFooter(config.getString("discord.update.embed.footer.text", "Report ID: %ReporterID%").replace("%ReporterID%", report.getReportId()),
                config.getString("discord.update.embed.footer.icon-url", "https://example.com/footer-icon.png"));
        embed.setImage(config.getString("discord.update.embed.image.url", "https://example.com/image.png"));
        embed.setThumbnail(config.getString("discord.update.embed.thumbnail.url", "https://example.com/thumbnail.png"));
        discordWebhook.addEmbed(embed);
        try {
            discordWebhook.execute();
        } catch (Exception e) {
            Bukkit.getLogger().severe("Failed to send report update to Discord: " + e.getMessage());
            ReportPlugin.getInstance().getLogger().severe("Failed to send report update to Discord: " + e.getMessage());
        }
    }
}