// src/main/java/ch/framedev/reportPlugin/commands/PlayerHeadsGUI.java
package ch.framedev.reportPlugin.commands;

import ch.framedev.reportPlugin.database.Database;
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

public record ReportDataCommand(Database database) implements CommandExecutor, Listener {

    private static final String GUI_TITLE = ChatColor.AQUA + "Report Data";
    private static final int PAGE_SIZE = 45; // 5 rows for heads, 1 row for navigation

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command.");
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

        Inventory gui = Bukkit.createInventory(null, size, GUI_TITLE + " (Page " + (page + 1) + "/" + totalPages + ")");

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
                    lore.add("§7No reports found for this player.");
                } else {
                    lore.add("§7Last Reported Reason: §6" + lastReport.getReason());
                    lore.add("§7Last Reported By: §6" + lastReport.getReporter());
                    lore.add("§7Last Reported At: §6" + new Date(lastReport.getTimestamp()).toString());
                    lore.add("§7Last Report Resolved: §6" + (lastReport.isResolved() ? "Yes" : "No"));
                    lore.add("§7Total Reports: §6" + database.countReportsForPlayer(p.getName()));
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
                prevMeta.setDisplayName(ChatColor.GREEN + "Previous Page");
                prev.setItemMeta(prevMeta);
            }
            gui.setItem(45, prev);
        }
        if (page < totalPages - 1) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = next.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
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
        if (!title.startsWith(GUI_TITLE)) return;
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
        if (meta.getDisplayName().contains("Previous Page")) {
            openPage(player, page - 1);
        } else if (meta.getDisplayName().contains("Next Page")) {
            openPage(player, page + 1);
        }
        // You can add actions for clicking player heads here
    }

    // Helper to extract page number from title
    private int getPageFromTitle(String title) {
        // Extract page number from title
        int idx = title.indexOf("Page ");
        if (idx < 0) return 0;
        String sub = title.substring(idx + 5);
        int slash = sub.indexOf("/");
        try {
            return Integer.parseInt(sub.substring(0, slash)) - 1;
        } catch (Exception e) {
            return 0;
        }
    }
}