package com.kaylves.interfacex.db.dao;

import java.sql.*;

public class ScanMetaDao {

    private final Connection connection;

    public ScanMetaDao(Connection connection) {
        this.connection = connection;
    }

    public Long getLastScanTime(String projectPath) throws SQLException {
        String sql = "SELECT last_scan_time FROM scan_meta WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("last_scan_time");
                }
            }
        }
        return null;
    }

    public void updateScanTime(String projectPath, long scanTime) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scan_meta (project_path, last_scan_time, scan_version) "
                + "VALUES (?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setLong(2, scanTime);
            ps.executeUpdate();
        }
    }

    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_meta WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }
}
