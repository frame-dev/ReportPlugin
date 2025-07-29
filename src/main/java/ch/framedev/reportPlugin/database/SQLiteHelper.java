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

public class SQLiteHelper implements DatabaseHelper {

    private final SQLite sqLite;

    public SQLiteHelper(ReportPlugin plugin) {
        String path = plugin.getConfig().getString("sqlite.path", plugin.getDataFolder() + "database");
        String databaseName = plugin.getConfig().getString("sqlite.database", "reports.db");
        if (path == null || databaseName == null) {
            throw new IllegalArgumentException("SQLite configuration is incomplete. Please check your config file.");
        }
        this.sqLite = new SQLite(path, databaseName);
        plugin.getLogger().info("Connecting to SQLite database at " + sqLite.getPath() + " with database " + sqLite.getDatabaseName());
        createTable();
        plugin.getLogger().info("SQLite database initialized successfully.");
    }

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
            ex.printStackTrace();
        }
    }

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
            ex.printStackTrace();
        }
    }

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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
        return false;
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
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
            ex.printStackTrace();
        }
    }
}
