// src/main/java/ch/framedev/reportPlugin/commands/PlayerHeadsGUI.java
package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
import ch.framedev.reportPlugin.utils.MessageUtils;
import ch.framedev.reportPlugin.utils.Report;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class ReportDataCommand implements CommandExecutor, Listener {

    private Database database;

    public ReportDataCommand(Database database) {
        this.database = database;
    }

    public void setDatabase(Database database) {
        this.database = database;
    }

    private static final int PAGE_SIZE = 45; // 5 rows for heads, 1 row for navigation

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            MessageUtils.send(sender, "messages.only_players_report_data", "&cOnly players can use this command.");
            return true;
        }
        if (!sender.hasPermission("reportplugin.reportdata")) {
            MessageUtils.send(sender, "messages.no_permission", "&cYou do not have permission to use this command.");
            return true;
        }
        openPage(player, 0);
        return true;
    }

    /**
     * Opens a paginated GUI showing player heads with report data.
     *
     * @param player The player to open the GUI for.
     * @param page   The page number to display (0-indexed).
     */
    private void openPage(Player player, int page) {
        List<? extends Player> online = Bukkit.getOnlinePlayers().stream().toList();
        int totalPages = (online.size() - 1) / PAGE_SIZE + 1;
        int size = 54; // 6 rows
        String guiTitle = MessageUtils.get("gui.titles.report_data", "&bReport Data");
        String fullTitle = MessageUtils.format("gui.titles.report_data_page", "{title} (Page {page}/{totalPages})",
                "{title}", guiTitle,
                "{page}", String.valueOf(page + 1),
                "{totalPages}", String.valueOf(totalPages));

        Inventory gui = Bukkit.createInventory(null, size, fullTitle);

        int start = page * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, online.size());

        for (int i = start, slot = 0; i < end; i++, slot++) {
            Player p = online.get(i);
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(p);
                meta.setDisplayName(ChatColor.YELLOW + p.getName());
                List<String> lore = new ArrayList<>();
                Report lastReport = database.getAllReports().stream()
                        .filter(r -> r.getReportedPlayer() != null
                                && r.getReportedPlayer().equalsIgnoreCase(p.getName()))
                        .max(Comparator.comparingLong(Report::getTimestamp))
                        .orElse(null);
                if (lastReport == null) {
                    lore.add(MessageUtils.get("gui.lores.report_data_no_reports", "&7No reports found for this player."));
                } else {
                    lore.add(MessageUtils.format("gui.lores.report_data_last_reason", "&7Last Reported Reason: &6{reason}",
                            "{reason}", lastReport.getReason()));
                    lore.add(MessageUtils.format("gui.lores.report_data_last_reporter", "&7Last Reported By: &6{reporter}",
                            "{reporter}", lastReport.getReporter()));
                    lore.add(MessageUtils.format("gui.lores.report_data_last_reported_at", "&7Last Reported At: &6{time}",
                            "{time}", String.valueOf(new Date(lastReport.getTimestamp()))));
                    lore.add(MessageUtils.format("gui.lores.report_data_last_status", "&7Last Report Status: &6{status}",
                            "{status}", lastReport.getStatus().getDisplayName()));
                    lore.add(MessageUtils.format("gui.lores.report_data_total_reports", "&7Total Reports: &6{count}",
                            "{count}", String.valueOf(database.countReportsForPlayer(p.getName()))));
                }
                meta.setLore(lore);
                head.setItemMeta(meta);
            }
            gui.setItem(slot, head);
        }

        // Navigation buttons
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prev.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(MessageUtils.get("gui.titles.previous_page", "&aPrevious Page"));
                prev.setItemMeta(prevMeta);
            }
            gui.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(MessageUtils.get("gui.titles.next_page", "&aNext Page"));
                next.setItemMeta(nextMeta);
            }
            gui.setItem(53, next);
        }

        player.openInventory(gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        String title = event.getView().getTitle();
        if (!title.startsWith(MessageUtils.get("gui.titles.report_data", "&bReport Data"))) return;
        event.setCancelled(true);

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        } else {
            meta.getDisplayName();
        }

        // Page navigation
        int page = getPageFromTitle(title);
        if (ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(
                MessageUtils.get("gui.titles.previous_page", "&aPrevious Page")))) {
            openPage(player, page - 1);
        } else if (ChatColor.stripColor(meta.getDisplayName()).equalsIgnoreCase(ChatColor.stripColor(
                MessageUtils.get("gui.titles.next_page", "&aNext Page")))) {
            openPage(player, page + 1);
        }
        // You can add actions for clicking player heads here
    }

    // Helper to extract page number from title
    private int getPageFromTitle(String title) {
        try {
            java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)/(\\d+)").matcher(title);
            if (!matcher.find()) {
                return 0;
            }
            return Integer.parseInt(matcher.group(1)) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
