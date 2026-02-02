package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.openapi.ui.Splitter;
import com.intellij.ui.JBColor;
import com.intellij.ui.ScrollPaneFactory;
import com.intellij.ui.treeStructure.SimpleTree;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.MatteBorder;

/**
 * @author kaylves
 */
public class InterfaceXNavigatorPanel extends SimpleToolWindowPanel {

    protected final Project project;

    SimpleTree simpleTree;

    Splitter rootSplitter;

    public InterfaceXNavigatorPanel(Project project,SimpleTree simpleTree) {
        super(true, true);
        this.project = project;
        this.simpleTree = simpleTree;
        initToolBar();

        initSimpleTree(simpleTree);
    }

    public void setBottomComponent(@Nullable JComponent component) {
        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(component);
        rootSplitter.setSecondComponent(scrollPane);
    }

    private void initSimpleTree(SimpleTree simpleTree) {
        this.simpleTree = simpleTree;

        simpleTree.setVisible(true);

        JScrollPane scrollPane = ScrollPaneFactory.createScrollPane(simpleTree);

        rootSplitter = new Splitter(true, .5f);
        rootSplitter.setShowDividerControls(true);
        rootSplitter.setDividerWidth(10);
        rootSplitter.setFirstComponent(scrollPane);

        setContent(rootSplitter);
    }

    private void initToolBar() {
        final ActionManager actionManager = ActionManager.getInstance();

        String actionId = "Toolkit.NavigatorActionsToolbar.InterfaceX";

        ActionToolbar actionToolbar = actionManager.createActionToolbar("InterfaceXNavigatorPanel Toolkit Navigator Toolbar",
                (DefaultActionGroup) actionManager.getAction(actionId), true);
        actionToolbar.setTargetComponent(this);
        setToolbar(actionToolbar.getComponent());
    }

}
