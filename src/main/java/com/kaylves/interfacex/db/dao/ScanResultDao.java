package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ScanResultEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScanResultDao {

    private final Connection connection;

    public ScanResultDao(Connection connection) {
        this.connection = connection;
    }

    public void batchUpsert(String projectPath, List<ScanResultEntity> entities) throws SQLException {
        String sql = "INSERT OR REPLACE INTO scan_result "
                + "(project_path, module_name, category, url, http_method, class_name, method_name, psi_element_hash, partner, scan_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (ScanResultEntity entity : entities) {
                ps.setString(1, entity.getProjectPath());
                ps.setString(2, entity.getModuleName());
                ps.setString(3, entity.getCategory());
                ps.setString(4, entity.getUrl());
                ps.setString(5, entity.getHttpMethod());
                ps.setString(6, entity.getClassName());
                ps.setString(7, entity.getMethodName());
                ps.setInt(8, entity.getPsiElementHash() != null ? entity.getPsiElementHash() : 0);
                ps.setString(9, entity.getPartner());
                ps.setLong(10, entity.getScanTime());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_result WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }

    public List<ScanResultEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM scan_result WHERE project_path = ?";
        List<ScanResultEntity> results = new ArrayList<>();
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

    private ScanResultEntity mapRow(ResultSet rs) throws SQLException {
        return ScanResultEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .moduleName(rs.getString("module_name"))
                .category(rs.getString("category"))
                .url(rs.getString("url"))
                .httpMethod(rs.getString("http_method"))
                .className(rs.getString("class_name"))
                .methodName(rs.getString("method_name"))
                .psiElementHash(rs.getInt("psi_element_hash"))
                .partner(rs.getString("partner"))
                .scanTime(rs.getLong("scan_time"))
                .build();
    }
}
