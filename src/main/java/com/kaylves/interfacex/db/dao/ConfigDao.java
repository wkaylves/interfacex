package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ConfigEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConfigDao {

    private final Connection connection;

    public ConfigDao(Connection connection) {
        this.connection = connection;
    }

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

    public void deleteByProjectPathAndKey(String projectPath, String configKey) throws SQLException {
        String sql = "DELETE FROM config WHERE project_path = ? AND config_key = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, configKey);
            ps.executeUpdate();
        }
    }

    private ConfigEntity mapRow(ResultSet rs) throws SQLException {
        return ConfigEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .configKey(rs.getString("config_key"))
                .configValue(rs.getString("config_value"))
                .updatedTime(rs.getLong("updated_time"))
                .build();
    }
}
