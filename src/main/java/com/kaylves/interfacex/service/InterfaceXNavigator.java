package com.kaylves.interfacex.service;

import com.intellij.openapi.wm.ToolWindow;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorPanel;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;
import com.kaylves.interfacex.utils.ToolkitUtil;
import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.openapi.wm.ex.ToolWindowManagerEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.SimpleTree;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.TreeSelectionModel;


/**
 * InterfaceXNavigator
 * <p>
 * 使用Service注解，不需要在plugin.xml中注册service节点了
 * 该注解可以自动注入Project对象
 *
 * @author kaylves
 * @since 1.0
 */
@Service(Service.Level.PROJECT)
@State(name = "InterfaceXNavigator", storages = {@Storage(StoragePathMacros.WORKSPACE_FILE)})
@Slf4j
public final class InterfaceXNavigator implements PersistentStateComponent<InterfaceXNavigatorState>{

    public static final Logger LOG = Logger.getInstance(InterfaceXNavigator.class);

    InterfaceXNavigatorState interfaceXNavigatorState = new InterfaceXNavigatorState();

    public static final String TOOL_WINDOW_ID = "InterfaceX";

    private final Project project;

    SimpleTree simpleTree;

    ToolWindowEx myToolWindow;

    InterfaceXSimpleTreeStructure interfaceXSimpleTreeStructure;

    public InterfaceXNavigator(Project project) {
        this.project = project;
    }


    public static InterfaceXNavigator getInstance(Project project) {
        return project.getService(InterfaceXNavigator.class);
    }


    private void initTree() {
        simpleTree = new SimpleTree();
        simpleTree.getEmptyText().clear();
        simpleTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }


    public void initToolWindow(ToolWindow toolWindow) {
        log.info("initToolWindow>>>>>>>>>");

        myToolWindow = (ToolWindowEx) toolWindow;

        initTree();


        final JPanel panel = new InterfaceXNavigatorPanel(project, simpleTree);

        final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);

        final Content content = contentFactory.createContent(panel, "", false);

        ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content, false);
    }


    public void initToolWindow() {

        log.info("initToolWindow>>>>>>>>>");

        final ToolWindowManagerEx manager = ToolWindowManagerEx.getInstanceEx(project);

        myToolWindow = (ToolWindowEx) manager.getToolWindow(TOOL_WINDOW_ID);
        if (myToolWindow != null) {
            return;
        }

        initTree();

        myToolWindow = (ToolWindowEx) manager.registerToolWindow(TOOL_WINDOW_ID, false, ToolWindowAnchor.RIGHT, project, true);

        myToolWindow.setIcon(ToolkitIcons.SERVICE);

        final JPanel panel = new InterfaceXNavigatorPanel(project, simpleTree);

        final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);

        final Content content = contentFactory.createContent(panel, "", false);

        ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content, false);
    }

    public void scheduleStructureUpdate() {
        scheduleStructureUpdate(false);
    }

    public void scheduleStructureUpdate(boolean needRefresh) {
        scheduleStructureRequest(() -> interfaceXSimpleTreeStructure.update(needRefresh));
    }

    private void scheduleStructureRequest(final Runnable runnable) {

        if (myToolWindow == null) {
            return;
        }

        ToolkitUtil.runWhenProjectIsReady(project, () -> {
            if (!myToolWindow.isVisible()) {
                return;
            }

            boolean shouldCreate = interfaceXSimpleTreeStructure == null;
            if (shouldCreate) {
                initStructure();
            }

            runnable.run();

//            // fixme: compat
//            if (shouldCreate) {
//                TreeState.createFrom(interfaceXNavigatorState.treeState).applyTo(simpleTree);
//            }

        });
    }

    private void initStructure() {
        interfaceXSimpleTreeStructure = new InterfaceXSimpleTreeStructure(project, simpleTree);
    }


    @Nullable
    @Override
    public InterfaceXNavigatorState getState() {
        if (interfaceXSimpleTreeStructure != null) {
            try {
                interfaceXNavigatorState.treeState = new Element("root");

                //======================================================================================
                // 2023.3 Threading Model Changes
                // https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html#-9rmqiu_24
                ApplicationManager.getApplication().invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        TreeState.createOn(simpleTree).writeExternal(interfaceXNavigatorState.treeState);
                    }
                });

            } catch (WriteExternalException e) {
                LOG.warn(e);
            }
        }
        return interfaceXNavigatorState;
    }

    @Override
    public void loadState(InterfaceXNavigatorState state) {
        interfaceXNavigatorState = state;
        scheduleStructureUpdate();
    }
}
