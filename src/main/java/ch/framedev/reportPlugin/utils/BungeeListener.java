package ch.framedev.reportPlugin.utils;

import ch.framedev.reportPlugin.main.ReportPlugin;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class BungeeListener {

    public BungeeListener(ReportPlugin plugin) {

    }

    public void sendLocation(Player player, Location location) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Teleport");
        out.writeUTF(player.getName());
        out.writeUTF(location.getWorld().getName());
        out.writeDouble(location.getX());
        out.writeDouble(location.getY());
        out.writeDouble(location.getZ());
        out.writeFloat(location.getYaw());
        out.writeFloat(location.getPitch());

        player.sendPluginMessage(ReportPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void requestData(Player player, String requestType) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("GetRequest");
        out.writeUTF(player.getName());
        out.writeUTF(requestType);

        player.sendPluginMessage(ReportPlugin.getInstance(), "BungeeCord", out.toByteArray());
    }
}
