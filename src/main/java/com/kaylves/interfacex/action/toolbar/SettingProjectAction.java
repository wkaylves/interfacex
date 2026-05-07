package com.kaylves.interfacex.action.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.db.storage.StorageType;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.InterfaceXSettingForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class SettingProjectAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(SettingProjectAction.class);

    @Nullable
    public static Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        LOG.warn("trigger:setting project");

        final Project project = getProject(e.getDataContext());

        InterfaceXSettingForm settingForm = new InterfaceXSettingForm();
        assert project != null;
        InterfaceXNavigator xNavigator = InterfaceXNavigator.getInstance(project);

        StorageAdapter adapter = StorageAdapter.getInstance();

        settingForm.setStorageType(adapter.getStorageType());
        settingForm.setUI(loadEnabledCategories(adapter));

        DialogBuilder dialogBuilder = getDialogBuilder(project, settingForm, xNavigator);
        dialogBuilder.show();
    }

    /**
     * 从 StorageAdapter 的 XML 持久化中加载已启用的接口类型
     */
    private static List<InterfaceItemConfigEntity> loadEnabledCategories(StorageAdapter adapter) {
        String enabledCategories = adapter.getEnabledCategories();
        if (enabledCategories == null || enabledCategories.isEmpty()) {
            return new ArrayList<>();
        }
        return Arrays.stream(enabledCategories.split(","))
                .filter(s -> !s.isEmpty())
                .map(category -> new InterfaceItemConfigEntity(category, true))
                .collect(Collectors.toList());
    }

    /**
     * 将已启用的接口类型列表转换为逗号分隔字符串
     */
    private static String toEnabledCategoriesString(List<InterfaceItemConfigEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return "";
        }
        return entities.stream()
                .filter(InterfaceItemConfigEntity::getEnabled)
                .map(InterfaceItemConfigEntity::getItemCategory)
                .collect(Collectors.joining(","));
    }

    private static @NotNull DialogBuilder getDialogBuilder(Project project, InterfaceXSettingForm settingForm, InterfaceXNavigator xNavigator) {
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(settingForm.getRootPanel());
        dialogBuilder.setTitle("设置");

        dialogBuilder.setOkOperation(() -> {
            StorageAdapter adapter = StorageAdapter.getInstance();
            String projectPath = project.getBasePath();

            StorageType newStorageType = settingForm.getStorageType();
            StorageType oldStorageType = adapter.getStorageType();

            if (oldStorageType != newStorageType) {
                xNavigator.switchStorageType(newStorageType);
            }

            List<InterfaceItemConfigEntity> selectedEntities = settingForm.getInterfaceItemConfigEntities();
            adapter.setEnabledCategories(toEnabledCategoriesString(selectedEntities));

            xNavigator.getXNavigatorState().setInterfaceItemConfigEntities(selectedEntities);

            adapter.saveConfig(ConfigEntity.builder()
                    .projectPath(projectPath)
                    .configKey("showPort")
                    .configValue(String.valueOf(xNavigator.getXNavigatorState().isShowPort()))
                    .updatedTime(System.currentTimeMillis())
                    .build());

            dialogBuilder.getDialogWrapper().doCancelAction();
        });
        return dialogBuilder;
    }
}
