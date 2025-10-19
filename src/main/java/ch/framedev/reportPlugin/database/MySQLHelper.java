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
import java.util.Optional;
import java.util.logging.Level;

public class MySQLHelper implements DatabaseHelper {

    private final MySQL mySQL;
    private final RedisManager redis;
    private final int cacheTtlSeconds;

    public MySQLHelper(ReportPlugin plugin) {
        String host = plugin.getConfig().getString("mysql.host", "localhost");
        String database = plugin.getConfig().getString("mysql.database", "database");
        String username = plugin.getConfig().getString("mysql.username", "root");
        String password = plugin.getConfig().getString("mysql.password", "password");
        int port = plugin.getConfig().getInt("mysql.port", 3306);
        this.mySQL = new MySQL(host, database, username, password, port);

        boolean redisEnabled = plugin.getConfig().getBoolean("redis.enabled", false);
        RedisManager tmpRedis = null;
        int ttl = plugin.getConfig().getInt("redis.ttl", 300);
        if (redisEnabled) {
            String rHost = plugin.getConfig().getString("redis.host", "localhost");
            int rPort = plugin.getConfig().getInt("redis.port", 6379);
            String rPass = plugin.getConfig().getString("redis.password", "");
            try {
                tmpRedis = new RedisManager(rHost, rPort, rPass);
                plugin.getLogger().info("Redis caching enabled (" + rHost + ":" + rPort + "), TTL=" + ttl + "s");
            } catch (Exception ex) {
                plugin.getLogger().warning("Failed to initialize Redis, continuing without cache: " + ex.getMessage());
            }
        }
        this.redis = tmpRedis;
        this.cacheTtlSeconds = ttl;

        plugin.getLogger().info("Connecting to MySQL database at " + host + ":" + port + " with database " + database);
        plugin.getLogger().info("Using username: " + username);
        plugin.getLogger().info("Using password: " + (password.isEmpty() ? "not set" : "********") + " (hidden for security reasons)");
        plugin.getLogger().info("Redis caching is " + (redisEnabled ? "enabled" : "disabled") + ".");
        plugin.getLogger().info("MySQL connection established successfully.");
        plugin.getLogger().info("Creating reports table...");
        createTable();
        plugin.getLogger().info("MySQL database initialized successfully.");
    }

    private String keyById(String id) {
        return "report:id:" + id;
    }

    private String keyByPlayer(String player) {
        return "report:player:" + player;
    }

    @Override
    public void createTable() {
        try {
            try (Connection connection = mySQL.connect()) {
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

                if (redis != null) {
                    redis.setObject(keyById(report.getReportId()), report, cacheTtlSeconds);
                    redis.setObject(keyByPlayer(report.getReportedPlayer()), report, cacheTtlSeconds);
                }
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
        if (redis != null) {
            try {
                Optional<Report> cached = redis.getObject(keyByPlayer(reportedPlayer), Report.class);
                if (cached.isPresent()) return cached.get();
            } catch (Exception ignored) {
            }
        }

        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports WHERE reported_player = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportedPlayer);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    Report r = new Gson().fromJson(data, Report.class);
                    if (redis != null) redis.setObject(keyByPlayer(reportedPlayer), r, cacheTtlSeconds);
                    return r;
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
        if (redis != null) {
            try {
                Optional<Report> cached = redis.getObject(keyById(reportId), Report.class);
                if (cached.isPresent()) return cached.get();
            } catch (Exception ignored) {
            }
        }

        try (Connection connection = mySQL.connect()) {
            if (connection != null) {
                String sql = "SELECT * FROM reports WHERE report_id = ?";
                var preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setString(1, reportId);
                var resultSet = preparedStatement.executeQuery();
                if (resultSet.next()) {
                    String data = resultSet.getString("data");
                    Report r = new Gson().fromJson(data, Report.class);
                    if (redis != null) redis.setObject(keyById(reportId), r, cacheTtlSeconds);
                    return r;
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

                if (redis != null) {
                    redis.setObject(keyById(report.getReportId()), report, cacheTtlSeconds);
                    redis.setObject(keyByPlayer(report.getReportedPlayer()), report, cacheTtlSeconds);
                }
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
                if (count > 0 && redis != null) {
                    // try to remove both id and player keys (player may be unknown here)
                    redis.del(keyById(reportId));
                }
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

    @Override
    public void disconnect() {
        if (redis != null) {
            try {
                redis.close();
            } catch (Exception ignored) {
            }
        }
        // MySQL connections are closed per-operation by design.
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
    public boolean isResolved(String reportId) {
        Report report = getReportById(reportId);
        return report != null && report.isResolved();
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