package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 21:30
 */

import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class H2StorageHelper implements DatabaseHelper {

    private final H2Storage h2Storage;

    /**
     * Initializes the h2StorageHelper with the specified ReportPlugin instance.
     * Connects to the h2Storage database using configuration from the plugin.
     *
     * @param plugin The ReportPlugin instance for logging and configuration access.
     */
    public H2StorageHelper(ReportPlugin plugin) {
        String path = plugin.getConfig().getString("h2Storage.path", plugin.getDataFolder() + "database");
        String databaseName = plugin.getConfig().getString("h2Storage.database", "reports.db");
        this.h2Storage = new H2Storage(path, databaseName);
        plugin.getLogger().info("Connecting to H2 database at " + h2Storage.getPath() + " with database " + h2Storage.getDatabaseName());
        plugin.getLogger().info("Creating reports table...");
        createTable();
        plugin.getLogger().info("H2 database initialized successfully.");
    }

    /**
     * Creates the reports table in the h2Storage database if it does not already exist.
     * The table includes columns for id, report_id, reported_player, reporter, and data.
     */
    public void createTable() {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "CREATE TABLE IF NOT EXISTS reports (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "report_id TEXT NOT NULL UNIQUE, " +
                        "reported_player TEXT NOT NULL, " +
                        "reporter TEXT NOT NULL, " +
                        "data TEXT NOT NULL" +
                        ")";
                connection.createStatement().executeUpdate(sql);
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error creating reports table", ex);
        }
    }

    /**
     * Inserts a new report into the reports table.
     *
     * @param report The Report object to be inserted into the database.
     */
    public void insertReport(Report report) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "INSERT INTO reports (report_id, reported_player, reporter, data) VALUES (?, ?, ?, ?)";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                preparedStatement.setString(2, report.getReportedPlayer());
                preparedStatement.setString(3, report.getReporter());
                preparedStatement.setString(4, report.toJson());
                preparedStatement.executeUpdate();
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error inserting report", ex);
        }
    }

    /**
     * Checks if a report with the specified report ID exists in the database.
     *
     * @param reportId The unique identifier of the report to check.
     * @return true if the report exists, false otherwise.
     */
    public boolean reportExists(String reportId) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT COUNT(*) FROM reports WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportId);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error checking if report exists", ex);
        }
        return false;
    }

    public boolean playerHasReport(String reportedPlayer) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT COUNT(*) FROM reports WHERE reported_player = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportedPlayer);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error checking if player has report", ex);
        }
        return false;
    }

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT COUNT(*) FROM reports WHERE reported_player = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportedPlayer);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error counting reports for player", ex);
        }
        return 0;
    }

    public List<Report> getAllReports() {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports";
                var preparedStatement = connection.prepareStatement(sql);
                var resultSet = preparedStatement.executeQuery();
                List<Report> reports = new java.util.ArrayList<>();
                while (resultSet.next()) {
                    String data = resultSet.getString("data");
                    reports.add(new Gson().fromJson(data, Report.class));
                }
                return reports;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error retrieving all reports", ex);
        }
        return java.util.Collections.emptyList();
    }

    public Report getReportByPlayer(String reportedPlayer) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports WHERE reported_player = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportedPlayer);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    return new Gson().fromJson(data, Report.class);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error retrieving report by player", ex);
        }
        return null;
    }

    public Report getReportByReporter(String reporter) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports WHERE reporter = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reporter);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    return new Gson().fromJson(data, Report.class);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error retrieving report by reporter", ex);
        }
        return null;
    }

    public Report getReportById(String reportId) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportId);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    return new Gson().fromJson(data, Report.class);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error retrieving report by ID", ex);
        }
        return null;
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        return getReportByPlayer(reportedPlayer);
    }

    @Override
    public boolean isResolved(String reportId) {
        Report report = getReportById(reportId);
        return report != null && report.isResolved();
    }

    public void updateReport(Report report) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "UPDATE reports SET reported_player = ?, reporter = ?, data = ? WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportedPlayer());
                preparedStatement.setString(2, report.getReporter());
                preparedStatement.setString(3, report.toJson());
                preparedStatement.setString(4, report.getReportId());
                preparedStatement.executeUpdate();
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error updating report", ex);
        }
    }

    public boolean deleteReport(String reportId) {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "DELETE FROM reports WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportId);
                int count = preparedStatement.executeUpdate();
                return count > 0;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error deleting report", ex);
        }
        return false;
    }

    @Override
    public boolean connect() {
        try(Connection connection = h2Storage.connect()) {
            return connection != null && !connection.isClosed();
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error connecting to h2Storage database", ex);
            return false;
        }
    }

    private void createUpdateHistoryTable() {
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "CREATE TABLE IF NOT EXISTS update_history (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "report_id TEXT NOT NULL, " +
                        "updated_at TEXT NOT NULL, " +
                        "update_data TEXT NOT NULL, " +
                        "FOREIGN KEY(report_id) REFERENCES reports(report_id)" +
                        ")";
                connection.createStatement().executeUpdate(sql);
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error creating update_history table", ex);
        }
    }

    @Override
    public boolean writeToUpdateHistory(Report report, String updater) {
        createUpdateHistoryTable();
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "INSERT INTO update_history (report_id, updated_at, update_data) VALUES (?, ?, ?)";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                preparedStatement.setString(2, java.time.Instant.now().toString());
                preparedStatement.setString(3, report.toJson());
                preparedStatement.executeUpdate();
                return true;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error writing to update history", ex);
        }
        return false;
    }

    @Override
    public Map<String, Report> getUpdateHistory(Report report) {
        createUpdateHistoryTable();
        Map<String, Report> history = new java.util.HashMap<>();
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "SELECT updated_at, update_data FROM update_history WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                var resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String updatedAt = resultSet.getString("updated_at");
                    String data = resultSet.getString("update_data");
                    Report historicalReport = new Gson().fromJson(data, Report.class);
                    history.put(updatedAt, historicalReport);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error retrieving update history", ex);
        }
        return history;
    }

    @Override
    public boolean clearUpdateHistory(Report report) {
        createUpdateHistoryTable();
        try (Connection connection = h2Storage.connect()) {
            if (connection != null) {
                String sql = "DELETE FROM update_history WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                int count = preparedStatement.executeUpdate();
                return count > 0;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error clearing update history", ex);
        }
        return false;
    }
}
