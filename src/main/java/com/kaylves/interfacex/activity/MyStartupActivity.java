package com.kaylves.interfacex.activity;

import com.kaylves.interfacex.navigator.InterfaceXNavigator;
import com.kaylves.interfacex.utils.ToolkitUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * 初始化ToolWindow ui Component
 * @author kaylves
 * @since 1.0
 */
@Slf4j
public class MyStartupActivity implements StartupActivity {

    private void init(@NotNull Project project) {

        log.info("project opened>>>>>>>>>");

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        InterfaceXNavigator interfaceXNavigator = InterfaceXNavigator.getInstance(project);
        ToolkitUtil.runWhenInitialized(project, () -> {

            if (project.isDisposed()) {
                return;
            }

            interfaceXNavigator.initToolWindow();

        });
    }

    @Override
    public void runActivity(@NotNull Project project) {
        init(project);
    }
}
