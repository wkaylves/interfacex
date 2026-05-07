package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ConfigEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 配置项数据访问对象
 * <p>提供 config 表的 CRUD 操作</p>
 * <p>config 表使用 (project_path, config_key) 作为联合主键，无自增 id</p>
 */
public class ConfigDao {

    private final Connection connection;

    public ConfigDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入或替换配置项
     * <p>基于联合主键 (project_path, config_key) 去重</p>
     */
    public void upsert(ConfigEntity entity) throws SQLException {
        String sql = "INSERT OR REPLACE INTO config (project_path, config_key, config_value, updated_time) "
                + "VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getProjectPath());
            ps.setString(2, entity.getConfigKey());
            ps.setString(3, entity.getConfigValue());
            ps.setLong(4, entity.getUpdatedTime());
            ps.executeUpdate();
        }
    }

    /**
     * 查询指定项目和键的配置值
     *
     * @param projectPath 项目路径
     * @param configKey   配置键
     * @return 配置值，不存在时返回 null
     */
    public String getValue(String projectPath, String configKey) throws SQLException {
        String sql = "SELECT config_value FROM config WHERE project_path = ? AND config_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, configKey);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("config_value");
                }
            }
        }
        return null;
    }

    /**
     * 查询指定项目的所有配置项
     */
    public List<ConfigEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM config WHERE project_path = ?";
        List<ConfigEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    /**
     * 删除指定项目和键的配置项
     */
    public void deleteByProjectPathAndKey(String projectPath, String configKey) throws SQLException {
        String sql = "DELETE FROM config WHERE project_path = ? AND config_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, configKey);
            ps.executeUpdate();
        }
    }

    /**
     * 将 ResultSet 行映射为 ConfigEntity
     * <p>注意：config 表无 id 列，联合主键为 (project_path, config_key)</p>
     */
    private ConfigEntity mapRow(ResultSet rs) throws SQLException {
        return ConfigEntity.builder()
                .projectPath(rs.getString("project_path"))
                .configKey(rs.getString("config_key"))
                .configValue(rs.getString("config_value"))
                .updatedTime(rs.getLong("updated_time"))
                .build();
    }
}
