package ch.framedev.reportPlugin.database;



/*
 * ch.framedev.reportPlugin
 * =============================================
 * This File was Created by FrameDev
 * Please do not change anything without my consent!
 * =============================================
 * This Class was created at 29.07.2025 21:29
 */

import ch.framedev.reportPlugin.utils.Report;
import ch.framedev.reportPlugin.main.ReportPlugin;
import com.google.gson.Gson;

import java.sql.Connection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class MySQLHelper implements DatabaseHelper {

    private final MySQL mySQL;

    /**
     * Initializes the MySQLHelper with the specified ReportPlugin instance.
     * Connects to the MySQL database using configuration from the plugin.
     *
     * @param plugin The ReportPlugin instance for logging and configuration access.
     */
    public MySQLHelper(ReportPlugin plugin) {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        String database = plugin.getConfig().getString("mysql.database", "database");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "password");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        this.mySQL = new MySQL(host, database, username, password, port);
        plugin.getLogger().info("Connecting to MySQL database at " + host + ":" + port + " with database " + database);
        plugin.getLogger().info("Using username: " + username);
        plugin.getLogger().info("Using password: " + (password.isEmpty() ? "not set" : "********") + " (hidden for security reasons)");
        plugin.getLogger().info("MySQL connection established successfully.");
        plugin.getLogger().info("Creating reports table...");
        createTable();
        plugin.getLogger().info("MySQL database initialized successfully.");
    }

    /**
     * Creates the reports table if it does not exist.
     */
    @Override
    public void createTable() {
        try {
            try(Connection connection = mySQL.connect()) {
                if (connection != null) {
                    String sql = "CREATE TABLE IF NOT EXISTS reports (" +
                                 "id INT AUTO_INCREMENT PRIMARY KEY, " +
                                 "report_id VARCHAR(255) NOT NULL UNIQUE, " +
                                 "reported_player VARCHAR(255) NOT NULL, " +
                                 "reporter VARCHAR(255) NOT NULL, " +
                                 "data TEXT NOT NULL" +
                                 ")";
                    connection.createStatement().executeUpdate(sql);
                } else {
                    System.err.println("Failed to connect to the database.");
                }
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while creating the reports table.", ex);
        }
    }

    /**
     * Inserts a report into the database.
     *
     * @param report The report to insert.
     */
    @Override
    public void insertReport(Report report) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while inserting a report.", ex);
        }
    }

    public boolean reportExists(String reportId) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while checking if a report exists.", ex);
        }
        return false;
    }

    public boolean playerHasReport(String reportedPlayer) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while checking if a player has a report.", ex);
        }
        return false;
    }

    @Override
    public int countReportsForPlayer(String reportedPlayer) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while counting reports for a player.", ex);
        }
        return 0;
    }

    public List<Report> getAllReports() {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while retrieving all reports.", ex);
        }
        return java.util.Collections.emptyList();
    }

    public Report getReportByPlayer(String reportedPlayer) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while retrieving a report by player.", ex);
        }
        return null;
    }

    public Report getReportByReporter(String reporter) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while retrieving a report by reporter.", ex);
        }
        return null;
    }

    public Report getReportById(String reportId) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while retrieving a report by ID.", ex);
        }
        return null;
    }

    @Override
    public Report getReportByReportedPlayer(String reportedPlayer) {
        return getReportByPlayer(reportedPlayer);
    }

    public void updateReport(Report report) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while updating a report.", ex);
        }
    }

    public boolean deleteReport(String reportId) {
        try (Connection connection = mySQL.connect()) {
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
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while deleting a report.", ex);
        }
        return false;
    }

    @Override
    public boolean connect() {
        try (Connection connection = mySQL.connect()) {
            return connection != null && !connection.isClosed();
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while connection to database", ex);
            return false;
        }
    }

    private void createUpdateHistoryTable() {
        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "CREATE TABLE IF NOT EXISTS report_update_history (" +
                             "id INT AUTO_INCREMENT PRIMARY KEY, " +
                             "report_id VARCHAR(255) NOT NULL, " +
                             "updater VARCHAR(255) NOT NULL, " +
                             "update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                             "data TEXT NOT NULL, " +
                             "FOREIGN KEY (report_id) REFERENCES reports(report_id) ON DELETE CASCADE" +
                             ")";
                connection.createStatement().executeUpdate(sql);
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while creating the report_update_history table.", ex);
        }
    }

    @Override
    public boolean writeToUpdateHistory(Report report, String updater) {
        createUpdateHistoryTable();
        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "INSERT INTO report_update_history (report_id, updater, data) VALUES (?, ?, ?)";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                preparedStatement.setString(2, updater);
                preparedStatement.setString(3, report.toJson());
                preparedStatement.executeUpdate();
                return true;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while writing to update history.", ex);
        }
        return false;
    }

    @Override
    public Map<String, Report> getUpdateHistory(Report report) {
        createUpdateHistoryTable();
        Map<String, Report> history = new java.util.HashMap<>();
        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM report_update_history WHERE report_id = ? ORDER BY update_time DESC";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                var resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    String updater = resultSet.getString("updater");
                    String data = resultSet.getString("data");
                    Report updatedReport = new Gson().fromJson(data, Report.class);
                    history.put(updater + " at " + resultSet.getTimestamp("update_time"), updatedReport);
                }
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while retrieving update history.", ex);
        }
        return history;
    }

    @Override
    public boolean clearUpdateHistory(Report report) {
        createUpdateHistoryTable();
        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "DELETE FROM report_update_history WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, report.getReportId());
                int count = preparedStatement.executeUpdate();
                return count > 0;
            } else {
                System.err.println("Failed to connect to the database.");
            }
        } catch (Exception ex) {
            ReportPlugin.getInstance().getLogger().log(Level.SEVERE, "An error occurred while clearing update history.", ex);
        }
        return false;
    }
}
