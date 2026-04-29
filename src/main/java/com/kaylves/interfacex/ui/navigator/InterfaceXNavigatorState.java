package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Data
@Slf4j
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    private List<InterfaceItemConfigEntity> interfaceItemConfigEntities;

    public void loadFromStorage(String projectPath) {
        StorageAdapter adapter = StorageAdapter.getInstance();

        List<ConfigEntity> configs = adapter.loadConfigs(projectPath);
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
    }
}
