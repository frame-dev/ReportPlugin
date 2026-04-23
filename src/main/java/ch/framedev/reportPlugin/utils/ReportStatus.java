package ch.framedev.reportPlugin.utils;

import java.util.Locale;

public enum ReportStatus {
    OPEN("Open", false),
    IN_PROGRESS("In Progress", false),
    RESOLVED("Resolved", true),
    REJECTED("Rejected", true),
    PUNISHED("Punished", true);

    private final String displayName;
    private final boolean closed;

    ReportStatus(String displayName, boolean closed) {
        this.displayName = displayName;
        this.closed = closed;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isClosed() {
        return closed;
    }

    public static ReportStatus fromStorage(String value) {
        if (value == null || value.isBlank()) {
            return OPEN;
        }

        String normalized = normalize(value);
        for (ReportStatus status : values()) {
            if (normalize(status.name()).equals(normalized)) {
                return status;
            }
        }
        return OPEN;
    }

    public static ReportStatus fromResolved(boolean resolved) {
        return resolved ? RESOLVED : OPEN;
    }

    public static ReportStatus parseUserInput(String input) {
        if (input == null || input.isBlank()) {
            return null;
        }

        String normalized = normalize(input);
        for (ReportStatus status : values()) {
            if (normalize(status.name()).equals(normalized)) {
                return status;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace('-', '_')
                .replace(' ', '_');
    }
}
