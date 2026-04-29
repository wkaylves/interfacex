package com.kaylves.interfacex.db.storage;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.model.TagEntity;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

/**
 * 存储后端统一接口
 * <p>定义所有存储实现必须提供的操作，使上层代码与具体存储方式解耦</p>
 * <p>当前实现：</p>
 * <ul>
 *   <li>{@link SqliteStorageBackend} - SQLite 数据库实现</li>
 *   <li>{@link XmlStorageBackend} - XML 文件实现</li>
 * </ul>
 */
public interface StorageBackend {

    /**
     * 初始化存储后端（如创建表、文件等）
     */
    void initialize();

    /**
     * 保存扫描结果（全量替换）
     *
     * @param projectPath 项目路径
     * @param entities    扫描结果列表
     */
    void saveScanResults(String projectPath, List<ScanResultEntity> entities) throws SQLException;

    /**
     * 加载指定项目的扫描结果
     */
    List<ScanResultEntity> loadScanResults(String projectPath) throws SQLException;

    /**
     * 删除指定项目的扫描结果
     */
    void deleteScanResults(String projectPath) throws SQLException;

    /**
     * 保存标签
     */
    void saveTag(TagEntity entity) throws SQLException;

    /**
     * 删除标签
     *
     * @param projectPath 项目路径
     * @param moduleName  模块名称
     * @param category    接口分类
     * @param url         接口 URL
     * @param httpMethod  HTTP 方法
     * @param methodName  方法名称
     * @param tagName     标签名称
     */
    void deleteTag(String projectPath, String moduleName, String category,
                   String url, String httpMethod, String methodName, String tagName) throws SQLException;

    /**
     * 加载指定项目的所有标签
     */
    List<TagEntity> loadTags(String projectPath) throws SQLException;

    /**
     * 按接口维度查询标签
     */
    List<TagEntity> loadTagsByInterface(String projectPath, String moduleName, String category,
                                         String url, String httpMethod, String methodName) throws SQLException;

    /**
     * 按标签名查询标签
     */
    List<TagEntity> loadTagsByTagName(String projectPath, String tagName) throws SQLException;

    /**
     * 保存配置项
     */
    void saveConfig(ConfigEntity entity) throws SQLException;

    /**
     * 查询配置值
     *
     * @return 配置值，不存在时返回 null
     */
    @Nullable
    String loadConfigValue(String projectPath, String configKey) throws SQLException;

    /**
     * 加载指定项目的所有配置项
     */
    List<ConfigEntity> loadConfigs(String projectPath) throws SQLException;

    /**
     * 保存扫描元数据（最后扫描时间）
     */
    void saveScanMeta(String projectPath, long scanTime) throws SQLException;

    /**
     * 获取最后扫描时间
     *
     * @return 时间戳，不存在时返回 null
     */
    @Nullable
    Long loadLastScanTime(String projectPath) throws SQLException;
}
