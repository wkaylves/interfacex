package com.kaylves.interfacex.service;

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.db.storage.StorageMigrator;
import com.kaylves.interfacex.db.storage.StorageType;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorPanel;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;
import com.kaylves.interfacex.utils.ToolkitUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.tree.TreeSelectionModel;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service(Service.Level.PROJECT)
@State(name = "InterfaceXNavigator", storages = {@Storage("InterfaceX.xml")})
@Slf4j
public final class InterfaceXNavigator implements PersistentStateComponent<InterfaceXNavigatorState>{

    @Getter
    InterfaceXNavigatorState xNavigatorState = new InterfaceXNavigatorState();

    @Getter
    private final Map<InterfaceItemCategoryEnum, InterfaceXForm> formConcurrentHashMap = new ConcurrentHashMap<>();

    public static final String TOOL_WINDOW_ID = "InterfaceX";

    private final Project project;

    private SimpleTree simpleTree;

    private ToolWindowEx myToolWindow;

    @Getter
    InterfaceXNavigatorPanel rootPanel;

    @Getter
    InterfaceXSimpleTreeStructure simpleTreeStructure;

    public InterfaceXNavigator(Project project) {
        this.project = project;

        String projectPath = project.getBasePath();

        xNavigatorState.loadFromStorage(projectPath);
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

        rootPanel = new InterfaceXNavigatorPanel(project, simpleTree);

        final ContentFactory contentFactory = ServiceManager.getService(ContentFactory.class);

        final Content content = contentFactory.createContent(rootPanel, "", false);

        ContentManager contentManager = myToolWindow.getContentManager();
        contentManager.addContent(content);
        contentManager.setSelectedContent(content, false);
    }

    public void scheduleStructureUpdate() {
        scheduleStructureUpdate(false);
    }

    public void scheduleStructureUpdate(boolean needRefresh) {
        scheduleStructureRequest(() -> simpleTreeStructure.update(needRefresh));
    }

    private void scheduleStructureRequest(final Runnable runnable) {
        if (myToolWindow == null) {
            return;
        }

        ToolkitUtil.runWhenProjectIsReady(project, () -> {
            if (!myToolWindow.isVisible()) {
                return;
            }

            boolean shouldCreate = simpleTreeStructure == null;
            if (shouldCreate) {
                initStructure();
            }

            runnable.run();
        });
    }

    private void initStructure() {
        simpleTreeStructure = new InterfaceXSimpleTreeStructure(project, simpleTree);
    }

    public void applyTagFilter(String tagName) {
        if (simpleTreeStructure != null) {
            simpleTreeStructure.applyTagFilter(tagName);
        }
    }

    public void clearTagFilter() {
        if (simpleTreeStructure != null) {
            simpleTreeStructure.clearTagFilter();
        }
    }

    public void switchStorageType(StorageType newType) {
        StorageAdapter adapter = StorageAdapter.getInstance();
        StorageType oldType = adapter.getStorageType();

        if (oldType == newType) {
            return;
        }

        String projectPath = project.getBasePath();
        boolean success = StorageMigrator.migrate(oldType, newType, projectPath);

        if (success) {
            adapter.setStorageType(newType);
            adapter.resetBackend();
            log.info("Storage type switched from {} to {}", oldType, newType);
        } else {
            log.error("Failed to switch storage type from {} to {}", oldType, newType);
        }
    }

    @Nullable
    @Override
    public InterfaceXNavigatorState getState() {
        return xNavigatorState;
    }

    @Override
    public void loadState(@NotNull InterfaceXNavigatorState state) {
        this.xNavigatorState = state;
        xNavigatorState.loadFromStorage(project.getBasePath());
        scheduleStructureUpdate();
    }
}
