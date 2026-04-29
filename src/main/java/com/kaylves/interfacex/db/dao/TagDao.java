package com.kaylves.interfacex.db.dao;

import com.kaylves.interfacex.db.model.TagEntity;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 标签数据访问对象
 * <p>提供 tag 表的 CRUD 操作，支持按项目、接口、标签名等多维度查询</p>
 */
public class TagDao {

    private final Connection connection;

    public TagDao(Connection connection) {
        this.connection = connection;
    }

    /**
     * 插入或替换标签
     * <p>基于联合唯一约束 (project_path, module_name, category, url, http_method, method_name, tag_name) 去重</p>
     */
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

    /**
     * 按接口维度精确删除标签
     *
     * @param projectPath 项目路径
     * @param moduleName  模块名称
     * @param category    接口分类
     * @param url         接口 URL
     * @param httpMethod  HTTP 方法
     * @param methodName  方法名称
     * @param tagName     标签名称
     */
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

    /**
     * 按项目路径查询所有标签
     */
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

    /**
     * 按接口维度查询标签（一个接口可有多个标签）
     *
     * @param projectPath 项目路径
     * @param moduleName  模块名称
     * @param category    接口分类
     * @param url         接口 URL
     * @param httpMethod  HTTP 方法
     * @param methodName  方法名称
     */
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

    /**
     * 按项目路径和标签名查询标签
     */
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

    /**
     * 按模块和分类查询去重的标签（仅返回 tagName 和 tagValue）
     */
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

    /**
     * 查询项目下所有去重的标签名
     */
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

    /**
     * 将 ResultSet 行映射为 TagEntity
     */
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
