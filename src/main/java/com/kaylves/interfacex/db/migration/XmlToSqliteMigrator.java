package com.kaylves.interfacex.db.migration;

import com.intellij.openapi.diagnostic.Logger;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;

import java.io.File;
import java.sql.SQLException;
import java.util.List;

public class XmlToSqliteMigrator {

    private static final Logger LOG = Logger.getInstance(XmlToSqliteMigrator.class);

    public static boolean needsMigration(String projectPath) {
        File xmlFile = new File(projectPath, ".idea/InterfaceX.xml");
        return xmlFile.exists();
    }

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
