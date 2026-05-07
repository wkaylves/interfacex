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

/**
 * 存储适配器（应用级单例 + 持久化配置）
 * <p>作为存储子系统的统一入口，封装了以下职责：</p>
 * <ul>
 *   <li>存储类型配置的持久化（通过 IntelliJ PersistentStateComponent）</li>
 *   <li>根据配置动态切换存储后端（SQLite / XML）</li>
 *   <li>统一异常处理，将 SQLException 转换为日志输出和默认返回值</li>
 * </ul>
 * <p>配置文件位置: ~/.config/JetBrains/interfacex-storage.xml</p>
 */
@Service(Service.Level.APP)
@State(name = "InterfaceXStorageConfig", storages = {@Storage("interfacex-storage.xml")})
@Slf4j
public class StorageAdapter implements PersistentStateComponent<StorageAdapter.State> {

    /**
     * 持久化状态 Bean
     * <p>由 IntelliJ 平台自动序列化/反序列化到 interfacex-storage.xml</p>
     * <p>包含：</p>
     * <ul>
     *   <li>storageType - 存储方式（SQLITE/XML）</li>
     *   <li>enabledCategories - 已启用的接口类型，逗号分隔（如 "HTTP,OpenFeign,RocketMQListener"）</li>
     * </ul>
     */
    @Getter
    @Setter
    static class State {
        /** 当前存储类型名称，默认 SQLITE */
        private String storageType = StorageType.SQLITE.name();
        /** 已启用的接口类型，逗号分隔；空字符串表示全部未启用 */
        private String enabledCategories = "";

        public State() {
        }
    }

    /** 当前存储类型 */
    @Getter
    private StorageType storageType = StorageType.SQLITE;

    /** 已启用的接口类型，逗号分隔 */
    @Getter
    private String enabledCategories = "";

    /** 当前活跃的存储后端实例，volatile 保证多线程可见性 */
    private volatile StorageBackend currentBackend;

    /**
     * 获取存储适配器单例
     */
    public static StorageAdapter getInstance() {
        return ApplicationManager.getApplication().getService(StorageAdapter.class);
    }

    /**
     * 切换存储类型
     * <p>切换后需要调用 {@link #resetBackend()} 或下次 {@link #getBackend()} 时自动重建</p>
     */
    public void setStorageType(StorageType type) {
        this.storageType = type;
        this.currentBackend = null;
    }

    /**
     * 设置已启用的接口类型
     *
     * @param categories 逗号分隔的接口类型名称
     */
    public void setEnabledCategories(String categories) {
        this.enabledCategories = categories != null ? categories : "";
    }

    /**
     * 获取当前存储后端实例
     * <p>使用双重检查锁定（DCL）保证线程安全的懒初始化</p>
     */
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

    /**
     * 重置后端实例，下次访问时重新创建
     */
    public void resetBackend() {
        this.currentBackend = null;
    }

    /**
     * 根据存储类型创建对应的后端实例
     */
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
        state.setEnabledCategories(enabledCategories);
        return state;
    }

    @Override
    public void loadState(State state) {
        this.storageType = StorageType.fromString(state.getStorageType());
        this.enabledCategories = state.getEnabledCategories() != null ? state.getEnabledCategories() : "";
    }
}
