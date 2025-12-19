package com.kaylves.interfacex.listener;

import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.ex.ToolWindowManagerListener;


/**
 *
 * @author kaylves
 * @since 1.0.0
 */
public class InterfaceXToolWindowListener implements ToolWindowManagerListener {

    private final Project project;

    public InterfaceXToolWindowListener(Project project) {
        this.project = project;
    }

    @Override
    public void stateChanged(ToolWindowManager toolWindowManager) {
        ToolWindow toolWindow = toolWindowManager.getToolWindow(
                InterfaceXNavigator.TOOL_WINDOW_ID
        );

        if (toolWindow == null) {
            return;
        }

        if (toolWindow.isDisposed()) {
            return;
        }

        boolean visible = toolWindow.isVisible();
        if (!visible) {
            return;
        }

        InterfaceXNavigator servicesNavigator = InterfaceXNavigator.getInstance(project);
        servicesNavigator.scheduleStructureUpdate();

    }
}
