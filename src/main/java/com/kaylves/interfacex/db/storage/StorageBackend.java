package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface StorageBackend {

    void saveScanResults(String projectPath, List<ScanResultEntity> entities) throws SQLException;

    List<ScanResultEntity> loadScanResults(String projectPath) throws SQLException;

    void deleteScanResults(String projectPath) throws SQLException;

    void saveTag(TagEntity entity) throws SQLException;

    void deleteTag(String projectPath, String moduleName, String category,
                   String url, String httpMethod, String methodName, String tagName) throws SQLException;

    List<TagEntity> loadTags(String projectPath) throws SQLException;

    List<TagEntity> loadTagsByInterface(String projectPath, String moduleName, String category,
                                         String url, String httpMethod, String methodName) throws SQLException;

    List<TagEntity> loadTagsByTagName(String projectPath, String tagName) throws SQLException;

    void saveConfig(ConfigEntity entity) throws SQLException;

    @Nullable
    String loadConfigValue(String projectPath, String configKey) throws SQLException;

    List<ConfigEntity> loadConfigs(String projectPath) throws SQLException;

    void saveScanMeta(String projectPath, long scanTime) throws SQLException;

    @Nullable
    Long loadLastScanTime(String projectPath) throws SQLException;

    void initialize();
}
