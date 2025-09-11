package com.kaylves.interfacex.action.toolbar;

import com.kaylves.interfacex.navigator.InterfaceXNavigator;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class RefreshProjectAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(RefreshProjectAction.class);


    @Nullable
    public static Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        LOG.warn("trigger:refresh project");
        final Project project = getProject(e.getDataContext());
        assert project != null;
        InterfaceXNavigator servicesNavigator = InterfaceXNavigator.getInstance(project);
        servicesNavigator.scheduleStructureUpdate(true);
    }
}
