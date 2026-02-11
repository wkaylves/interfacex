package com.kaylves.interfacex.service;

import com.intellij.ide.util.treeView.TreeState;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorPanel;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;
import com.kaylves.interfacex.utils.ToolkitUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeSelectionModel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


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

    @Getter
    InterfaceXNavigatorState interfaceXNavigatorState = new InterfaceXNavigatorState();

    @Getter
    private Map<InterfaceXItemCategoryEnum, InterfaceXForm> formCache = new ConcurrentHashMap<>();

    public static final String TOOL_WINDOW_ID = "InterfaceX";

    private final Project project;

    private SimpleTree simpleTree;

    private ToolWindowEx myToolWindow;

    @Getter
    InterfaceXNavigatorPanel rootPannel;

    @Getter
    InterfaceXSimpleTreeStructure interfaceXSimpleTreeStructure;

    public InterfaceXNavigator(Project project) {
        this.project = project;
    }


    public static InterfaceXNavigator getInstance(Project project) {
        return project.getService(InterfaceXNavigator.class);
    }


    private void createSimpleTree() {
        simpleTree = new SimpleTree();
        simpleTree.getEmptyText().clear();
        simpleTree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
    }


    public void initToolWindow(ToolWindow toolWindow) {
        log.info("initToolWindow>>>>>>>>>");

        myToolWindow = (ToolWindowEx) toolWindow;

        createSimpleTree();

        rootPannel = new InterfaceXNavigatorPanel(project, simpleTree);

        final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);

        final Content content = contentFactory.createContent(rootPannel, "", false);

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

//             fixme: compat
            if(shouldCreate){
                TreeState.createFrom(interfaceXNavigatorState.treeState).applyTo(simpleTree);
            }

            runnable.run();
        });
    }

    private void initStructure() {
        interfaceXSimpleTreeStructure = new InterfaceXSimpleTreeStructure(project, simpleTree);
    }


    @Nullable
    @Override
    public InterfaceXNavigatorState getState() {
        return interfaceXNavigatorState;
    }

    @Override
    public void loadState(InterfaceXNavigatorState state) {
        interfaceXNavigatorState = state;
        scheduleStructureUpdate();
    }
}
