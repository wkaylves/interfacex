package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

@Slf4j
public class StorageMigrator {

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

    private static StorageBackend createBackend(StorageType type, String projectPath) {
        switch (type) {
            case XML:
                return new XmlStorageBackend(projectPath);
            case SQLITE:
            default:
                return new SqliteStorageBackend();
        }
    }

    private static void migrateScanResults(StorageBackend source, StorageBackend target,
                                            String projectPath) throws SQLException {
        List<ScanResultEntity> results = source.loadScanResults(projectPath);
        if (!results.isEmpty()) {
            target.saveScanResults(projectPath, results);
            log.info("Migrated {} scan results", results.size());
        }
    }

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

    private static void migrateScanMeta(StorageBackend source, StorageBackend target,
                                         String projectPath) throws SQLException {
        Long lastScanTime = source.loadLastScanTime(projectPath);
        if (lastScanTime != null) {
            target.saveScanMeta(projectPath, lastScanTime);
            log.info("Migrated scan meta: lastScanTime={}", lastScanTime);
        }
    }

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
                log.info("Cleaned up SQLite scan results for project: {}", projectPath);
            } catch (SQLException e) {
                log.warn("Failed to clean up SQLite data after migration", e);
            }
        }
    }
}
