package com.kaylves.interfacex.db.migration;

import com.intellij.openapi.diagnostic.Logger;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

/**
 * XML 到 SQLite 数据迁移工具
 * <p>将旧版 .idea/InterfaceX.xml 中的配置数据迁移到 SQLite config 表</p>
 * <p>迁移完成后自动删除 XML 文件</p>
 * <p>迁移内容：</p>
 * <ul>
 *   <li>showPort 配置项</li>
 *   <li>InterfaceItemConfigEntity 各分类的启用状态</li>
 * </ul>
 */
public class XmlToSqliteMigrator {

    private static final Logger LOG = Logger.getInstance(XmlToSqliteMigrator.class);

    /**
     * 检查是否需要迁移
     * <p>当项目中存在 .idea/InterfaceX.xml 文件时表示需要迁移</p>
     *
     * @param projectPath 项目根路径
     * @return true 表示存在旧版 XML 文件需要迁移
     */
    public static boolean needsMigration(String projectPath) {
        File xmlFile = new File(projectPath, ".idea/InterfaceX.xml");
        return xmlFile.exists();
    }

    /**
     * 执行 XML 到 SQLite 的迁移
     * <p>迁移步骤：</p>
     * <ol>
     *   <li>初始化数据库服务</li>
     *   <li>将 showPort 配置写入 config 表</li>
     *   <li>将各分类启用状态写入 config 表（key 格式: itemCategory.{category}）</li>
     *   <li>删除旧版 XML 文件</li>
     * </ol>
     *
     * @param projectPath    项目根路径
     * @param showPort       是否显示端口号
     * @param configEntities 旧版接口分类配置列表
     */
    public static void migrate(String projectPath, boolean showPort,
                                List<InterfaceItemConfigEntity> configEntities) {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        ConfigDao configDao = dbService.getConfigDao();

        try {
            ConfigEntity showPortEntity = ConfigEntity.builder()
                    .projectPath(projectPath)
                    .configKey("showPort")
                    .configValue(String.valueOf(showPort))
                    .updatedTime(System.currentTimeMillis())
                    .build();
            configDao.upsert(showPortEntity);

            if (configEntities != null) {
                for (InterfaceItemConfigEntity entity : configEntities) {
                    ConfigEntity configEntity = ConfigEntity.builder()
                            .projectPath(projectPath)
                            .configKey("itemCategory." + entity.getItemCategory())
                            .configValue(String.valueOf(entity.getEnabled()))
                            .updatedTime(System.currentTimeMillis())
                            .build();
                    configDao.upsert(configEntity);
                }
            }

            File xmlFile = new File(projectPath, ".idea/InterfaceX.xml");
            if (xmlFile.exists()) {
                boolean deleted = xmlFile.delete();
                LOG.info("XML migration completed, file deleted: " + deleted);
            }
        } catch (SQLException e) {
            LOG.error("Failed to migrate XML to SQLite", e);
        }
    }
}
