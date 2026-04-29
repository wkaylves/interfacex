package com.kaylves.interfacex.action.toolbar;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ConfigDao;
import com.kaylves.interfacex.db.model.ConfigEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.InterfaceXSettingForm;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

/**
 * @author kaylves
 */
public class SettingProjectAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(SettingProjectAction.class);


    @Nullable
    public static Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        LOG.warn("trigger:refresh project");

        final Project project = getProject(e.getDataContext());

        InterfaceXSettingForm settingForm = new InterfaceXSettingForm();
        assert project != null;
        InterfaceXNavigator xNavigator =InterfaceXNavigator.getInstance(project);
        settingForm.setUI(xNavigator.getXNavigatorState().getInterfaceItemConfigEntities());

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

            InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
            ConfigDao configDao = dbService.getConfigDao();
            String projectPath = project.getBasePath();

            try {
                configDao.upsert(ConfigEntity.builder()
                        .projectPath(projectPath)
                        .configKey("showPort")
                        .configValue(String.valueOf(xNavigatorState.isShowPort()))
                        .updatedTime(System.currentTimeMillis())
                        .build());

                for (InterfaceItemConfigEntity entity : xNavigatorState.getInterfaceItemConfigEntities()) {
                    configDao.upsert(ConfigEntity.builder()
                            .projectPath(projectPath)
                            .configKey("itemCategory." + entity.getItemCategory())
                            .configValue(String.valueOf(entity.getEnabled()))
                            .updatedTime(System.currentTimeMillis())
                            .build());
                }
            } catch (SQLException ex) {
                LOG.error("Failed to save config to SQLite", ex);
            }

            dialogBuilder.getDialogWrapper().doCancelAction();

        });
        return dialogBuilder;
    }
}
