package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author kaylves
 */
@Data
@Slf4j
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    private List<InterfaceItemConfigEntity> interfaceItemConfigEntities;

    public void loadFromDatabase(String projectPath) {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        ConfigDao configDao = dbService.getConfigDao();

        try {
            List<ConfigEntity> configs = configDao.findByProjectPath(projectPath);
            List<InterfaceItemConfigEntity> entities = new ArrayList<>();

            for (ConfigEntity config : configs) {
                if ("showPort".equals(config.getConfigKey())) {
                    this.showPort = Boolean.parseBoolean(config.getConfigValue());
                } else if (config.getConfigKey().startsWith("itemCategory.")) {
                    String category = config.getConfigKey().substring("itemCategory.".length());
                    boolean enabled = Boolean.parseBoolean(config.getConfigValue());
                    entities.add(new InterfaceItemConfigEntity(category, enabled));
                }
            }

            if (!entities.isEmpty()) {
                this.interfaceItemConfigEntities = entities;
            }
        } catch (SQLException e) {
            log.error("Failed to load config from SQLite", e);
        }
    }
}
