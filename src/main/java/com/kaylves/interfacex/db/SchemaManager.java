package com.kaylves.interfacex.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * 数据库 Schema 版本管理器
 * <p>负责数据库表的创建和版本迁移，采用版本号递增策略</p>
 * <p>当前版本: 1，包含 scan_result、tag、scan_meta、config 四张表</p>
 */
@Slf4j
public class SchemaManager {

    /** 当前数据库 Schema 版本号 */
    private static final int CURRENT_VERSION = 1;

    /**
     * 初始化数据库 Schema
     * <p>创建 schema_version 表并执行未应用的迁移脚本</p>
     *
     * @param conn 数据库连接
     */
    public void initialize(Connection conn) throws SQLException {
        createSchemaVersionTable(conn);
        int currentVersion = getCurrentVersion(conn);
        if (currentVersion < CURRENT_VERSION) {
            applyMigrations(conn, currentVersion, CURRENT_VERSION);
        }
    }

    /**
     * 创建 Schema 版本记录表
     */
    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_version ("
                + "version INTEGER PRIMARY KEY"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    /**
     * 获取当前已应用的 Schema 版本号
     *
     * @return 版本号，空表时返回 0
     */
    private int getCurrentVersion(Connection conn) throws SQLException {
        String sql = "SELECT MAX(version) FROM schema_version";
        try (var rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * 依次应用从 fromVersion+1 到 toVersion 的迁移脚本
     */
    private void applyMigrations(Connection conn, int fromVersion, int toVersion) throws SQLException {
        for (int v = fromVersion + 1; v <= toVersion; v++) {
            applyMigration(conn, v);
        }
    }

    /**
     * 应用单个版本的迁移脚本
     */
    private void applyMigration(Connection conn, int version) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            switch (version) {
                case 1 -> applyV1(stmt);
                default -> throw new SQLException("Unknown migration version: " + version);
            }
            stmt.execute("INSERT INTO schema_version (version) VALUES (" + version + ")");
        }
        log.info("Applied schema migration v{}", version);
    }

    /**
     * V1 迁移：创建所有基础表和索引
     * <ul>
     *   <li>scan_result - 扫描结果表，联合唯一约束 (project_path, module_name, category, url, http_method, method_name)</li>
     *   <li>tag - 标签表，联合唯一约束 (project_path, module_name, category, url, http_method, method_name, tag_name)</li>
     *   <li>scan_meta - 扫描元数据表，主键 project_path</li>
     *   <li>config - 配置项表，联合主键 (project_path, config_key)，无自增 id</li>
     * </ul>
     */
    private void applyV1(Statement stmt) throws SQLException {
        stmt.execute("CREATE TABLE IF NOT EXISTS scan_result ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_path TEXT NOT NULL,"
                + "module_name TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "url TEXT NOT NULL,"
                + "http_method TEXT,"
                + "class_name TEXT,"
                + "method_name TEXT,"
                + "psi_element_hash INTEGER,"
                + "partner TEXT,"
                + "scan_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, module_name, category, url, http_method, method_name)"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS tag ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "project_path TEXT NOT NULL,"
                + "module_name TEXT NOT NULL,"
                + "category TEXT NOT NULL,"
                + "url TEXT NOT NULL,"
                + "http_method TEXT,"
                + "method_name TEXT NOT NULL,"
                + "tag_name TEXT NOT NULL,"
                + "tag_value TEXT,"
                + "created_time INTEGER NOT NULL,"
                + "updated_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, module_name, category, url, http_method, method_name, tag_name)"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS scan_meta ("
                + "project_path TEXT PRIMARY KEY,"
                + "last_scan_time INTEGER NOT NULL,"
                + "scan_version INTEGER NOT NULL DEFAULT 1"
                + ")");

        stmt.execute("CREATE TABLE IF NOT EXISTS config ("
                + "project_path TEXT NOT NULL,"
                + "config_key TEXT NOT NULL,"
                + "config_value TEXT NOT NULL,"
                + "updated_time INTEGER NOT NULL,"
                + "UNIQUE(project_path, config_key)"
                + ")");

        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scan_project ON scan_result(project_path)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_scan_category ON scan_result(project_path, category)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_tag_project ON tag(project_path)");
        stmt.execute("CREATE INDEX IF NOT EXISTS idx_tag_name ON tag(tag_name)");
    }
}
