// src/main/java/ch/framedev/spigotTest/ReportGUI.java
package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.*;
import org.bukkit.command.*;
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

        for (int i = 0; i < Math.min(reports.size(), size - 2); i++) {
            Report report = reports.get(i);
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Report: " + report.getReportId());
                meta.setLore(List.of(
                        ChatColor.GRAY + "Player: " + report.getReportedPlayer(),
                        ChatColor.GRAY + "Reporter: " + report.getReporter(),
                        ChatColor.GRAY + "Reason: " + report.getReason()
                ));
                item.setItemMeta(meta);
            }
            gui.setItem(i, item);
        }

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
        if (meta == null || meta.getDisplayName() == null) return;

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        UUID uuid = player.getUniqueId();

        if (displayName != null && displayName.startsWith("Report: ")) {
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
        } else if (displayName != null && displayName.equals(ChatColor.stripColor(UPDATE_TITLE))) {
            if (!playerSelectedReport.containsKey(uuid)) {
                player.sendMessage(ChatColor.RED + "Select a report first by clicking on it.");
                return;
            }
            UpdateSession session = new UpdateSession();
            session.reportId = playerSelectedReport.get(uuid);
            updateSessions.put(uuid, session);
            player.closeInventory();
            player.sendMessage(ChatColor.GOLD + "Enter the new reason for the report:");
        } else if (displayName != null && displayName.equals(ChatColor.stripColor(TELEPORT_TITLE))) {
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
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!updateSessions.containsKey(uuid)) return;

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
                break;
        }
    }
}