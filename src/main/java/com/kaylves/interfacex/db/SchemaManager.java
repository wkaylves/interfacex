package com.kaylves.interfacex.db;

import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class SchemaManager {

    private static final int CURRENT_VERSION = 1;

    public void initialize(Connection conn) throws SQLException {
        createSchemaVersionTable(conn);
        int currentVersion = getCurrentVersion(conn);
        if (currentVersion < CURRENT_VERSION) {
            applyMigrations(conn, currentVersion, CURRENT_VERSION);
        }
    }

    private void createSchemaVersionTable(Connection conn) throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS schema_version ("
                + "version INTEGER PRIMARY KEY"
                + ")";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }
    }

    private int getCurrentVersion(Connection conn) throws SQLException {
        String sql = "SELECT MAX(version) FROM schema_version";
        try (var rs = conn.createStatement().executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    private void applyMigrations(Connection conn, int fromVersion, int toVersion) throws SQLException {
        for (int v = fromVersion + 1; v <= toVersion; v++) {
            applyMigration(conn, v);
        }
    }

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
