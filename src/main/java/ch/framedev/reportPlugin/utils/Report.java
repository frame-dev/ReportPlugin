package ch.framedev.reportPlugin.utils;



/*
 * ch.framedev.spigotTest
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 18:10
 */

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class Report {

    private String reportedPlayer;
    private String reason;
    private String reporter;
    private long timestamp;
    private boolean resolved;
    private String resolutionComment;
    private String reportId;
    private String serverName;
    private String serverIp;
    private String serverVersion;
    private String worldName;
    private String location; // Format: "world,x,y,z"
    private String additionalInfo;

    public Report(String reportedPlayer, String reason, String reporter, String reportId, String serverName, String serverIp, String serverVersion, String worldName, String location) {
        this.reportedPlayer = reportedPlayer;
        this.reason = reason;
        this.reporter = reporter;
        this.timestamp = System.currentTimeMillis();
        this.resolved = false;
        this.resolutionComment = "";
        this.reportId = reportId;
        this.serverName = serverName;
        this.serverIp = serverIp;
        this.serverVersion = serverVersion;
        this.worldName = worldName;
        this.location = location;
        this.additionalInfo = "";
    }

    public Report(String reportedPlayer, String reason, String reporter, String reportId, String serverName, String serverIp, String serverVersion, String worldName, Location location) {
        this(reportedPlayer, reason, reporter, reportId, serverName, serverIp, serverVersion, worldName, getLocationAsString(location));
    }

    public Report(String reportedPlayer, String reason, String reporter, String reportId, String serverName, String serverIp, String serverVersion, String worldName, Location location, String additionalInfo) {
        this(reportedPlayer, reason, reporter, reportId, serverName, serverIp, serverVersion, worldName, getLocationAsString(location));
        this.additionalInfo = additionalInfo;
    }

    /**
     * Constructs a Report object from a MongoDB Document.
     *
     * @param document the Document containing report data.
     */
    public Report(Document document) {
        this.reportedPlayer = document.getString("reportedPlayer");
        this.reason = document.getString("reason");
        this.reporter = document.getString("reporter");
        this.timestamp = document.getLong("timestamp");
        this.resolved = document.getBoolean("resolved", false);
        this.resolutionComment = document.getString("resolutionComment");
        this.reportId = document.getString("reportId");
        this.serverName = document.getString("serverName");
        this.serverIp = document.getString("serverIp");
        this.serverVersion = document.getString("serverVersion");
        this.worldName = document.getString("worldName");
        this.location = document.getString("location");
        this.additionalInfo = document.getString("additionalInfo");
    }

    public String getReportedPlayer() {
        return reportedPlayer;
    }

    public void setReportedPlayer(String reportedPlayer) {
        this.reportedPlayer = reportedPlayer;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReporter() {
        return reporter;
    }

    public void setReporter(String reporter) {
        this.reporter = reporter;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerVersion() {
        return serverVersion;
    }

    public void setServerVersion(String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public static String getLocationAsString(Location location) {
        if (location == null || location.getWorld() == null) {
            throw new IllegalArgumentException("Location or world cannot be null");
        }
        return location.getWorld().getName() + "," + location.getX() + "," + location.getY() + "," + location.getZ();
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public static Location getLocationAsBukkitLocation(String location) {
        String[] parts = location.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid location format: " + location);
        }
        return new Location(Bukkit.getWorld(parts[0]), Double.parseDouble(parts[1]), Double.parseDouble(parts[2]), Double.parseDouble(parts[3]));
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    @Override
    public String toString() {
        return "Report{" +
               "reportedPlayer='" + reportedPlayer + '\'' +
               ", reason='" + reason + '\'' +
               ", reporter='" + reporter + '\'' +
               ", timestamp=" + timestamp +
               ", resolved=" + resolved +
               ", resolutionComment='" + resolutionComment + '\'' +
               ", reportId='" + reportId + '\'' +
               ", serverName='" + serverName + '\'' +
               ", serverIp='" + serverIp + '\'' +
               ", serverVersion='" + serverVersion + '\'' +
               ", worldName='" + worldName + '\'' +
               ", location='" + location + '\'' +
               ", additionalInfo='" + additionalInfo + '\'' +
               '}';
    }

    /**
     * Converts this Report object to a MongoDB Document.
     *
     * @return a Document representation of this Report.
     */
    public Document toDocument() {
        Document document = new Document();
        document.put("reportedPlayer", reportedPlayer);
        document.put("reason", reason);
        document.put("reporter", reporter);
        document.put("timestamp", timestamp);
        document.put("resolved", resolved);
        document.put("resolutionComment", resolutionComment);
        document.put("reportId", reportId);
        document.put("serverName", serverName);
        document.put("serverIp", serverIp);
        document.put("serverVersion", serverVersion);
        document.put("worldName", worldName);
        document.put("location", location);
        document.put("additionalInfo", additionalInfo);
        return document;
    }
}
