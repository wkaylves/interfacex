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
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        settingForm.setUI(xNavigator.getXNavigatorState().getInterfaceItemConfigEntities());

        StorageAdapter adapter = StorageAdapter.getInstance();
        settingForm.setStorageType(adapter.getStorageType());

        DialogBuilder dialogBuilder = getDialogBuilder(project, settingForm, xNavigator);
        dialogBuilder.show();
    }

    private static @NotNull DialogBuilder getDialogBuilder(Project project, InterfaceXSettingForm settingForm, InterfaceXNavigator xNavigator) {
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        dialogBuilder.setCenterPanel(settingForm.getRootPanel());
        dialogBuilder.setTitle("设置");

        dialogBuilder.setOkOperation(() -> {
            InterfaceXNavigatorState xNavigatorState = xNavigator.getXNavigatorState();
            xNavigatorState.setInterfaceItemConfigEntities(settingForm.getInterfaceItemConfigEntities());

            StorageAdapter adapter = StorageAdapter.getInstance();
            String projectPath = project.getBasePath();

            StorageType newStorageType = settingForm.getStorageType();
            StorageType oldStorageType = adapter.getStorageType();

            if (oldStorageType != newStorageType) {
                xNavigator.switchStorageType(newStorageType);
            }

            adapter.saveConfig(ConfigEntity.builder()
                    .projectPath(projectPath)
                    .configKey("showPort")
                    .configValue(String.valueOf(xNavigatorState.isShowPort()))
                    .updatedTime(System.currentTimeMillis())
                    .build());

            for (InterfaceItemConfigEntity entity : xNavigatorState.getInterfaceItemConfigEntities()) {
                adapter.saveConfig(ConfigEntity.builder()
                        .projectPath(projectPath)
                        .configKey("itemCategory." + entity.getItemCategory())
                        .configValue(String.valueOf(entity.getEnabled()))
                        .updatedTime(System.currentTimeMillis())
                        .build());
            }

            dialogBuilder.getDialogWrapper().doCancelAction();
        });
        return dialogBuilder;
    }
}
