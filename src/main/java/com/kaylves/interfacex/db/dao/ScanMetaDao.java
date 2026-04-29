package com.kaylves.interfacex.db.dao;

import java.sql.*;

/**
 * 扫描元数据数据访问对象
 * <p>提供 scan_meta 表的 CRUD 操作，记录每个项目的最后扫描时间</p>
 * <p>scan_meta 表以 project_path 为主键</p>
 */
public class ScanMetaDao {

    private final Connection connection;

    public ScanMetaDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 获取指定项目的最后扫描时间
     *
     * @param projectPath 项目路径
     * @return 最后扫描时间戳，不存在时返回 null
     */
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

    /**
     * 更新或插入扫描时间
     * <p>使用 INSERT OR REPLACE 语义</p>
     *
     * @param projectPath 项目路径
     * @param scanTime    扫描时间戳
     */
    public void updateScanTime(String projectPath, long scanTime) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scan_meta (project_path, last_scan_time, scan_version) "
                + "VALUES (?, ?, 1)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setLong(2, scanTime);
            ps.executeUpdate();
        }
    }

    /**
     * 删除指定项目的扫描元数据
     */
    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_meta WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }
}
