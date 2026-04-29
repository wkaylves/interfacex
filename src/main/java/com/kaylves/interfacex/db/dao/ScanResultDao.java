package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.ScanResultEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 扫描结果数据访问对象
 * <p>提供 scan_result 表的 CRUD 操作，支持批量插入和按项目路径查询</p>
 */
public class ScanResultDao {

    private final Connection connection;

    public ScanResultDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 批量插入或替换扫描结果
     * <p>使用 INSERT OR REPLACE 语义，基于联合唯一约束去重</p>
     *
     * @param projectPath 项目路径（冗余参数，实际从 entity 获取）
     * @param entities    待持久化的扫描结果列表
     */
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

    /**
     * 按项目路径删除所有扫描结果
     */
    public void deleteByProjectPath(String projectPath) throws SQLException {
        String sql = "DELETE FROM scan_result WHERE project_path = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.executeUpdate();
        }
    }

    /**
     * 按项目路径查询所有扫描结果
     */
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

    /**
     * 将 ResultSet 行映射为 ScanResultEntity
     */
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
