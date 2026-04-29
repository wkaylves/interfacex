package com.kaylves.interfacex.db.storage;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

@Service(Service.Level.APP)
@State(name = "InterfaceXStorageConfig", storages = {@Storage("interfacex-storage.xml")})
@Slf4j
public class StorageAdapter implements PersistentStateComponent<StorageAdapter.State> {

    @Getter
    @Setter
    static class State {
        private String storageType = StorageType.SQLITE.name();

        public State() {
        }
    }

    @Getter
    private StorageType storageType = StorageType.SQLITE;

    private volatile StorageBackend currentBackend;

    public static StorageAdapter getInstance() {
        return ApplicationManager.getApplication().getService(StorageAdapter.class);
    }

    public void setStorageType(StorageType type) {
        this.storageType = type;
        this.currentBackend = null;
    }

    public StorageBackend getBackend() {
        if (currentBackend == null) {
            synchronized (this) {
                if (currentBackend == null) {
                    currentBackend = createBackend(storageType);
                    currentBackend.initialize();
                }
            }
        }
        return currentBackend;
    }

    public void resetBackend() {
        this.currentBackend = null;
    }

    private StorageBackend createBackend(StorageType type) {
        switch (type) {
            case XML:
                return new XmlStorageBackend();
            case SQLITE:
            default:
                return new SqliteStorageBackend();
        }
    }

    public void saveScanResults(String projectPath, List<ScanResultEntity> entities) {
        try {
            getBackend().saveScanResults(projectPath, entities);
        } catch (SQLException e) {
            log.error("Failed to save scan results", e);
        }
    }

    public List<ScanResultEntity> loadScanResults(String projectPath) {
        try {
            return getBackend().loadScanResults(projectPath);
        } catch (SQLException e) {
            log.error("Failed to load scan results", e);
            return List.of();
        }
    }

    public void deleteScanResults(String projectPath) {
        try {
            getBackend().deleteScanResults(projectPath);
        } catch (SQLException e) {
            log.error("Failed to delete scan results", e);
        }
    }

    public void saveTag(TagEntity entity) {
        try {
            getBackend().saveTag(entity);
        } catch (SQLException e) {
            log.error("Failed to save tag", e);
        }
    }

    public void deleteTag(String projectPath, String moduleName, String category,
                          String url, String httpMethod, String methodName, String tagName) {
        try {
            getBackend().deleteTag(projectPath, moduleName, category, url, httpMethod, methodName, tagName);
        } catch (SQLException e) {
            log.error("Failed to delete tag", e);
        }
    }

    public List<TagEntity> loadTags(String projectPath) {
        try {
            return getBackend().loadTags(projectPath);
        } catch (SQLException e) {
            log.error("Failed to load tags", e);
            return List.of();
        }
    }

    public List<TagEntity> loadTagsByInterface(String projectPath, String moduleName, String category,
                                                String url, String httpMethod, String methodName) {
        try {
            return getBackend().loadTagsByInterface(projectPath, moduleName, category, url, httpMethod, methodName);
        } catch (SQLException e) {
            log.error("Failed to load tags by interface", e);
            return List.of();
        }
    }

    public List<TagEntity> loadTagsByTagName(String projectPath, String tagName) {
        try {
            return getBackend().loadTagsByTagName(projectPath, tagName);
        } catch (SQLException e) {
            log.error("Failed to load tags by name", e);
            return List.of();
        }
    }

    public void saveConfig(ConfigEntity entity) {
        try {
            getBackend().saveConfig(entity);
        } catch (SQLException e) {
            log.error("Failed to save config", e);
        }
    }

    @Nullable
    public String loadConfigValue(String projectPath, String configKey) {
        try {
            return getBackend().loadConfigValue(projectPath, configKey);
        } catch (SQLException e) {
            log.error("Failed to load config value", e);
            return null;
        }
    }

    public List<ConfigEntity> loadConfigs(String projectPath) {
        try {
            return getBackend().loadConfigs(projectPath);
        } catch (SQLException e) {
            log.error("Failed to load configs", e);
            return List.of();
        }
    }

    public void saveScanMeta(String projectPath, long scanTime) {
        try {
            getBackend().saveScanMeta(projectPath, scanTime);
        } catch (SQLException e) {
            log.error("Failed to save scan meta", e);
        }
    }

    @Nullable
    public Long loadLastScanTime(String projectPath) {
        try {
            return getBackend().loadLastScanTime(projectPath);
        } catch (SQLException e) {
            log.error("Failed to load last scan time", e);
            return null;
        }
    }

    @Nullable
    @Override
    public State getState() {
        State state = new State();
        state.setStorageType(storageType.name());
        return state;
    }

    @Override
    public void loadState(State state) {
        this.storageType = StorageType.fromString(state.getStorageType());
    }
}
