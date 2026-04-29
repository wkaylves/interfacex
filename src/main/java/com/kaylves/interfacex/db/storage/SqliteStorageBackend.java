package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.dao.ScanMetaDao;
import com.kaylves.interfacex.db.dao.ScanResultDao;
import com.kaylves.interfacex.db.dao.TagDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

@Slf4j
public class SqliteStorageBackend implements StorageBackend {

    private final InterfaceXDatabaseService dbService;

    public SqliteStorageBackend() {
        this.dbService = InterfaceXDatabaseService.getInstance();
    }

    @Override
    public void initialize() {
        dbService.initialize();
    }

    private ScanResultDao scanResultDao() {
        return dbService.getScanResultDao();
    }

    private TagDao tagDao() {
        return dbService.getTagDao();
    }

    private ConfigDao configDao() {
        return dbService.getConfigDao();
    }

    private ScanMetaDao scanMetaDao() {
        return dbService.getScanMetaDao();
    }

    @Override
    public void saveScanResults(String projectPath, List<ScanResultEntity> entities) throws SQLException {
        scanResultDao().deleteByProjectPath(projectPath);
        if (!entities.isEmpty()) {
            scanResultDao().batchUpsert(projectPath, entities);
        }
    }

    @Override
    public List<ScanResultEntity> loadScanResults(String projectPath) throws SQLException {
        return scanResultDao().findByProjectPath(projectPath);
    }

    @Override
    public void deleteScanResults(String projectPath) throws SQLException {
        scanResultDao().deleteByProjectPath(projectPath);
    }

    @Override
    public void saveTag(TagEntity entity) throws SQLException {
        tagDao().insert(entity);
    }

    @Override
    public void deleteTag(String projectPath, String moduleName, String category,
                          String url, String httpMethod, String methodName, String tagName) throws SQLException {
        tagDao().delete(projectPath, moduleName, category, url, httpMethod, methodName, tagName);
    }

    @Override
    public List<TagEntity> loadTags(String projectPath) throws SQLException {
        return tagDao().findByProjectPath(projectPath);
    }

    @Override
    public List<TagEntity> loadTagsByInterface(String projectPath, String moduleName, String category,
                                                String url, String httpMethod, String methodName) throws SQLException {
        return tagDao().findByInterface(projectPath, moduleName, category, url, httpMethod, methodName);
    }

    @Override
    public List<TagEntity> loadTagsByTagName(String projectPath, String tagName) throws SQLException {
        return tagDao().findByTagName(projectPath, tagName);
    }

    @Override
    public void saveConfig(ConfigEntity entity) throws SQLException {
        configDao().upsert(entity);
    }

    @Nullable
    @Override
    public String loadConfigValue(String projectPath, String configKey) throws SQLException {
        return configDao().getValue(projectPath, configKey);
    }

    @Override
    public List<ConfigEntity> loadConfigs(String projectPath) throws SQLException {
        return configDao().findByProjectPath(projectPath);
    }

    @Override
    public void saveScanMeta(String projectPath, long scanTime) throws SQLException {
        scanMetaDao().updateScanTime(projectPath, scanTime);
    }

    @Nullable
    @Override
    public Long loadLastScanTime(String projectPath) throws SQLException {
        return scanMetaDao().getLastScanTime(projectPath);
    }
}
