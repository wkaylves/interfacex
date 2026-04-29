package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.TagEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDao {

    private final Connection connection;

    public TagDao(Connection connection) {
        this.connection = connection;
    }

    public void insert(TagEntity entity) throws SQLException {
        String sql = "INSERT OR REPLACE INTO tag "
                + "(project_path, module_name, category, url, http_method, method_name, tag_name, tag_value, created_time, updated_time) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, entity.getProjectPath());
            ps.setString(2, entity.getModuleName());
            ps.setString(3, entity.getCategory());
            ps.setString(4, entity.getUrl());
            ps.setString(5, entity.getHttpMethod());
            ps.setString(6, entity.getMethodName());
            ps.setString(7, entity.getTagName());
            ps.setString(8, entity.getTagValue());
            ps.setLong(9, entity.getCreatedTime());
            ps.setLong(10, entity.getUpdatedTime());
            ps.executeUpdate();
        }
    }

    public void delete(String projectPath, String moduleName, String category,
                       String url, String httpMethod, String methodName, String tagName) throws SQLException {
        String sql = "DELETE FROM tag WHERE project_path = ? AND module_name = ? AND category = ? "
                + "AND url = ? AND http_method = ? AND method_name = ? AND tag_name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, moduleName);
            ps.setString(3, category);
            ps.setString(4, url);
            ps.setString(5, httpMethod);
            ps.setString(6, methodName);
            ps.setString(7, tagName);
            ps.executeUpdate();
        }
    }

    public List<TagEntity> findByProjectPath(String projectPath) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ?";
        List<TagEntity> results = new ArrayList<>();
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

    public List<TagEntity> findByInterface(String projectPath, String moduleName, String category,
                                            String url, String httpMethod, String methodName) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ? AND module_name = ? AND category = ? "
                + "AND url = ? AND http_method = ? AND method_name = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, moduleName);
            ps.setString(3, category);
            ps.setString(4, url);
            ps.setString(5, httpMethod);
            ps.setString(6, methodName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<TagEntity> findByTagName(String projectPath, String tagName) throws SQLException {
        String sql = "SELECT * FROM tag WHERE project_path = ? AND tag_name = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, tagName);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapRow(rs));
                }
            }
        }
        return results;
    }

    public List<TagEntity> findByCategory(String projectPath, String moduleName, String category) throws SQLException {
        String sql = "SELECT DISTINCT tag_name, tag_value FROM tag WHERE project_path = ? AND module_name = ? AND category = ?";
        List<TagEntity> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            ps.setString(2, moduleName);
            ps.setString(3, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TagEntity entity = new TagEntity();
                    entity.setTagName(rs.getString("tag_name"));
                    entity.setTagValue(rs.getString("tag_value"));
                    results.add(entity);
                }
            }
        }
        return results;
    }

    public List<String> findAllTagNames(String projectPath) throws SQLException {
        String sql = "SELECT DISTINCT tag_name FROM tag WHERE project_path = ? ORDER BY tag_name";
        List<String> results = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, projectPath);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(rs.getString("tag_name"));
                }
            }
        }
        return results;
    }

    private TagEntity mapRow(ResultSet rs) throws SQLException {
        return TagEntity.builder()
                .id(rs.getLong("id"))
                .projectPath(rs.getString("project_path"))
                .moduleName(rs.getString("module_name"))
                .category(rs.getString("category"))
                .url(rs.getString("url"))
                .httpMethod(rs.getString("http_method"))
                .methodName(rs.getString("method_name"))
                .tagName(rs.getString("tag_name"))
                .tagValue(rs.getString("tag_value"))
                .createdTime(rs.getLong("created_time"))
                .updatedTime(rs.getLong("updated_time"))
                .build();
    }
}
