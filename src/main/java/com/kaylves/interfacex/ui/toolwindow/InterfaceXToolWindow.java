package com.kaylves.interfacex.ui.toolwindow;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXToolWindow implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        log.info("project opened>>>>>>>>>");

        if (ApplicationManager.getApplication().isUnitTestMode()) {
            return;
        }

        ApplicationManager.getApplication().invokeLater(() -> {
            InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);

            if (project.isDisposed()) {
                return;
            }

            navigator.initToolWindow(toolWindow);
        });
    }
}
