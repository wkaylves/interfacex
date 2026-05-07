package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Slf4j
public class InterfaceXNavigatorState {

    public boolean showPort = true;

    private List<InterfaceItemConfigEntity> interfaceItemConfigEntities;

    /**
     * 从持久化存储加载配置
     * <p>接口类型设置从 StorageAdapter（XML 持久化）加载</p>
     * <p>其它配置项（如 showPort）从当前存储后端加载</p>
     *
     * @param projectPath 项目路径
     */
    public void loadFromStorage(String projectPath) {
        StorageAdapter adapter = StorageAdapter.getInstance();

        loadEnabledCategories(adapter);
        loadOtherConfigs(adapter, projectPath);
    }

    /**
     * 从 StorageAdapter 的 XML 持久化中加载已启用的接口类型
     */
    private void loadEnabledCategories(StorageAdapter adapter) {
        String enabledCategories = adapter.getEnabledCategories();
        if (enabledCategories == null || enabledCategories.isEmpty()) {
            this.interfaceItemConfigEntities = new ArrayList<>();
            return;
        }

        this.interfaceItemConfigEntities = Arrays.stream(enabledCategories.split(","))
                .filter(s -> !s.isEmpty())
                .map(category -> new InterfaceItemConfigEntity(category, true))
                .collect(Collectors.toList());
    }

    /**
     * 从当前存储后端加载其它配置项（如 showPort）
     */
    private void loadOtherConfigs(StorageAdapter adapter, String projectPath) {
        List<ConfigEntity> configs = adapter.loadConfigs(projectPath);
        for (ConfigEntity config : configs) {
            if ("showPort".equals(config.getConfigKey())) {
                this.showPort = Boolean.parseBoolean(config.getConfigValue());
            }
        }
    }
}
