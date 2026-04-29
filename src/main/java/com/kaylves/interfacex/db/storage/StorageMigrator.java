package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * 存储迁移工具
 * <p>支持在 XML 和 SQLite 两种存储后端之间进行数据迁移</p>
 * <p>迁移流程：</p>
 * <ol>
 *   <li>从源后端读取所有数据（扫描结果、标签、配置、元数据）</li>
 *   <li>将数据写入目标后端</li>
 *   <li>清理源后端数据</li>
 * </ol>
 */
@Slf4j
public class StorageMigrator {

    /**
     * 执行存储迁移
     *
     * @param from        源存储类型
     * @param to          目标存储类型
     * @param projectPath 项目路径
     * @return true 迁移成功，false 迁移失败
     */
    public static boolean migrate(StorageType from, StorageType to, String projectPath) {
        if (from == to) {
            log.info("Storage type unchanged, skipping migration");
            return true;
        }

        log.info("Starting storage migration: {} -> {}, projectPath={}", from, to, projectPath);

        StorageBackend sourceBackend = createBackend(from, projectPath);
        StorageBackend targetBackend = createBackend(to, projectPath);

        sourceBackend.initialize();
        targetBackend.initialize();

        try {
            migrateScanResults(sourceBackend, targetBackend, projectPath);
            migrateTags(sourceBackend, targetBackend, projectPath);
            migrateConfigs(sourceBackend, targetBackend, projectPath);
            migrateScanMeta(sourceBackend, targetBackend, projectPath);

            cleanupSource(from, projectPath);

            log.info("Storage migration completed: {} -> {}", from, to);
            return true;
        } catch (SQLException e) {
            log.error("Storage migration failed: {} -> {}", from, to, e);
            return false;
        }
    }

    /**
     * 根据存储类型创建后端实例
     */
    private static StorageBackend createBackend(StorageType type, String projectPath) {
        switch (type) {
            case XML:
                return new XmlStorageBackend(projectPath);
            case SQLITE:
            default:
                return new SqliteStorageBackend();
        }
    }

    /**
     * 迁移扫描结果
     */
    private static void migrateScanResults(StorageBackend source, StorageBackend target,
                                            String projectPath) throws SQLException {
        List<ScanResultEntity> results = source.loadScanResults(projectPath);
        if (!results.isEmpty()) {
            target.saveScanResults(projectPath, results);
            log.info("Migrated {} scan results", results.size());
        }
    }

    /**
     * 迁移标签
     */
    private static void migrateTags(StorageBackend source, StorageBackend target,
                                     String projectPath) throws SQLException {
        List<TagEntity> tags = source.loadTags(projectPath);
        for (TagEntity tag : tags) {
            target.saveTag(tag);
        }
        if (!tags.isEmpty()) {
            log.info("Migrated {} tags", tags.size());
        }
    }

    /**
     * 迁移配置项
     */
    private static void migrateConfigs(StorageBackend source, StorageBackend target,
                                        String projectPath) throws SQLException {
        List<ConfigEntity> configs = source.loadConfigs(projectPath);
        for (ConfigEntity config : configs) {
            target.saveConfig(config);
        }
        if (!configs.isEmpty()) {
            log.info("Migrated {} configs", configs.size());
        }
    }

    /**
     * 迁移扫描元数据
     */
    private static void migrateScanMeta(StorageBackend source, StorageBackend target,
                                         String projectPath) throws SQLException {
        Long lastScanTime = source.loadLastScanTime(projectPath);
        if (lastScanTime != null) {
            target.saveScanMeta(projectPath, lastScanTime);
            log.info("Migrated scan meta: lastScanTime={}", lastScanTime);
        }
    }

    /**
     * 迁移完成后清理源后端数据
     * <p>XML: 删除数据文件和配置文件</p>
     * <p>SQLite: 删除该项目的扫描结果</p>
     */
    private static void cleanupSource(StorageType from, String projectPath) {
        if (from == StorageType.XML) {
            File xmlFile = new File(projectPath, ".idea/InterfaceX-data.xml");
            if (xmlFile.exists()) {
                boolean deleted = xmlFile.delete();
                log.info("Cleaned up XML data file: deleted={}", deleted);
            }
            File oldXmlFile = new File(projectPath, ".idea/InterfaceX.xml");
            if (oldXmlFile.exists()) {
                boolean deleted = oldXmlFile.delete();
                log.info("Cleaned up old XML config file: deleted={}", deleted);
            }
        } else if (from == StorageType.SQLITE) {
            try {
                SqliteStorageBackend sqliteBackend = new SqliteStorageBackend();
                sqliteBackend.initialize();
                sqliteBackend.deleteScanResults(projectPath);
                for (TagEntity tag : sqliteBackend.loadTags(projectPath)) {
                    sqliteBackend.deleteTag(tag.getProjectPath(), tag.getModuleName(), tag.getCategory(),
                            tag.getUrl(), tag.getHttpMethod(), tag.getMethodName(), tag.getTagName());
                }
                log.info("Cleaned up SQLite data for project: {}", projectPath);
            } catch (SQLException e) {
                log.warn("Failed to clean up SQLite data after migration", e);
            }
        }
    }
}
