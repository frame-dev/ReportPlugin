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
import java.util.logging.Level;

public class SQLiteHelper implements DatabaseHelper {

    private final SQLite sqLite;

    /**
     * Initializes the SQLiteHelper with the specified ReportPlugin instance.
     * Connects to the SQLite database using configuration from the plugin.
     *
     * @param plugin The ReportPlugin instance for logging and configuration access.
     */
    public SQLiteHelper(ReportPlugin plugin) {
        String path = plugin.getConfig().getString("sqlite.path", plugin.getDataFolder() + "database");
        String databaseName = plugin.getConfig().getString("sqlite.database", "reports.db");
        this.sqLite = new SQLite(path, databaseName);
        plugin.getLogger().info("Connecting to SQLite database at " + sqLite.getPath() + " with database " + sqLite.getDatabaseName());
        plugin.getLogger().info("Creating reports table...");
        createTable();
        plugin.getLogger().info("SQLite database initialized successfully.");
    }

    /**
     * Creates the reports table in the SQLite database if it does not already exist.
     * The table includes columns for id, report_id, reported_player, reporter, and data.
     */
    public void createTable() {
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
            if (connection != null) {
                String sql = "INSERT INTO reports (report_id, reported_player, reporter, data) VALUES (?, ?, ?, ?)";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                preparedStatement.setString(2, report.getReportedPlayer());
                preparedStatement.setString(3, report.getReporter());
                preparedStatement.setString(4, new Gson().toJson(report));
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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
        try (Connection connection = sqLite.connect()) {
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

    public void updateReport(Report report) {
        try (Connection connection = sqLite.connect()) {
            if (connection != null) {
                String sql = "UPDATE reports SET reported_player = ?, reporter = ?, data = ? WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportedPlayer());
                preparedStatement.setString(2, report.getReporter());
                preparedStatement.setString(3, new Gson().toJson(report));
                preparedStatement.setString(4, report.getReportId());
                preparedStatement.executeUpdate();
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error updating report", ex);
        }
    }

    public void deleteReport(String reportId) {
        try (Connection connection = sqLite.connect()) {
            if (connection != null) {
                String sql = "DELETE FROM reports WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportId);
                preparedStatement.executeUpdate();
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "Error deleting report", ex);
        }
    }
}
