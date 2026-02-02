package com.kaylves.interfacex.ui.navigator;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.todo.TodoTreeBuilder;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.ui.tree.AsyncTreeModel;
import com.intellij.ui.tree.StructureTreeModel;
import com.intellij.ui.treeStructure.CachingSimpleNode;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.intellij.ui.treeStructure.SimpleTreeStructure;
import com.intellij.util.OpenSourceUtil;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.service.ProjectInitService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXSimpleTreeStructure extends SimpleTreeStructure {

    RootNode myRoot = new RootNode();

    private final Map<RestServiceProject, ProjectNode> myProjectToNodeMapping = new ConcurrentHashMap<>(1);

    protected Project project;

    protected SimpleTree simpleTree;

    StructureTreeModel<?> structureTreeModel;

    public InterfaceXSimpleTreeStructure(Project project, SimpleTree simpleTree) {
        this.project = project;

        this.simpleTree = simpleTree;

        structureTreeModel = new StructureTreeModel<>(this, TodoTreeBuilder.NODE_DESCRIPTOR_COMPARATOR, this.project);

        AsyncTreeModel asyncTreeModel = new AsyncTreeModel(structureTreeModel, this.project);
        simpleTree.setModel(asyncTreeModel);

        InterfaceXPopupMenu interfaceXPopupMenu = new InterfaceXPopupMenu(simpleTree,project);
        interfaceXPopupMenu.installPopupMenu();
    }



    /**
     * 打开所有树子节点
     */
    public void expandAll() {
        DefaultTreeExpander myTreeExpander = new DefaultTreeExpander(simpleTree);
        myTreeExpander.expandAll();
    }

    @Override
    public @NotNull Object getRootElement() {
        return myRoot;
    }

    public void update(boolean needRefresh) {

        if (!needRefresh) {
            return;
        }

        ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> ReadAction.run(() -> {

            List<RestServiceProject> projects = ProjectInitService.getInstance(project).getServiceProjects();
            updateProjects(projects);
            // 同步方式兼容旧版:ml-citation{ref="2" data="citationList"}
            structureTreeModel.invalidate();
        }),"InterfaceX Scanning Implementations...",true,project);

    }

    public void updateProjects(List<RestServiceProject> projects) {

        for (RestServiceProject each : projects) {

            ProjectNode node = findNodeFor(each);

            if (node == null) {
                node = new ProjectNode(myRoot, each);
                myProjectToNodeMapping.put(each, node);
            }
        }

        myRoot.childrenChanged();
        myRoot.updateProjectNodes(projects);
    }

    private ProjectNode findNodeFor(RestServiceProject project) {
        return myProjectToNodeMapping.get(project);
    }

    public void updateFrom(SimpleNode node) {
        if (node == null) {
            return;
        }
    }

    private void updateUpTo(SimpleNode node) {
        if (node == null) {
            return;
        }

        SimpleNode each = node;

        while (each != null) {
            SimpleNode parent = each.getParent();
            updateFrom(each);
            each = each.getParent();
        }
    }

    public abstract class BaseSimpleNode extends CachingSimpleNode {

        protected BaseSimpleNode(SimpleNode parent) {
            super(parent);
        }

        @Override
        public void cleanUpCache() {
            super.cleanUpCache();
        }

        protected void childrenChanged() {
            BaseSimpleNode each = this;
            while (each != null) {
                each.cleanUpCache();
                each = (BaseSimpleNode) each.getParent();
            }
            updateUpTo(this);
        }

        protected String getMenuId() {
            return "Toolkit.NavigatorServiceMenu";
        }
    }

    public class RootNode extends BaseSimpleNode {

        List<ProjectNode> projectNodes = new ArrayList<>();

        protected RootNode() {
            super(null);
            getTemplatePresentation().setIcon(ToolkitIcons.SERVICE);
            setIcon(ToolkitIcons.SERVICE); //兼容 IDEA 2016
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return projectNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return "InterfaceX";
        }

        public void updateProjectNodes(List<RestServiceProject> projects) {
            projectNodes.clear();

            for (RestServiceProject project : projects) {
                ProjectNode projectNode = new ProjectNode(this, project);
                projectNodes.add(projectNode);
            }

            updateFrom(getParent());

            childrenChanged();

            updateUpTo(this);
        }
    }

    public class ProjectNode extends BaseSimpleNode {

        List<CategoryNode> categoryNodes = new ArrayList<>();

        final RestServiceProject myProject;

        public ProjectNode(SimpleNode parent, RestServiceProject project) {

            super(parent);
            myProject = project;

            getTemplatePresentation().setIcon(ToolkitIcons.MODULE);

            //兼容 IDEA 2016
            setIcon(ToolkitIcons.MODULE);

            updateServiceNodes(project.getServiceItemMap());
        }

        private void updateServiceNodes(Map<String, List<ServiceItem>> serviceItems) {

            serviceItems.forEach((s, restServiceItems) -> {
                categoryNodes.add(new CategoryNode(s, this, myProject));
            });


            SimpleNode parent = getParent();

            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }

            updateFrom(parent);
        }

        protected SimpleNode[] buildChildren() {
            return categoryNodes.toArray(new SimpleNode[0]);
        }

        @Override
        public String getName() {
            return myProject.getModuleName();
        }

    }

    public class CategoryNode extends BaseSimpleNode {

        List<ServiceNode> serviceNodes = new ArrayList<>();

        final RestServiceProject myProject;

        private final String type;

        public CategoryNode(String type, SimpleNode parent, RestServiceProject project) {

            super(parent);
            this.type = type;
            myProject = project;

            getTemplatePresentation().setIcon(ToolkitIcons.SERVICE);
            setIcon(ToolkitIcons.SERVICE); //兼容 IDEA 2016

            updateServiceNodes(project.getServiceItemMap().get(type));
        }

        private void updateServiceNodes(List<ServiceItem> serviceItems) {
            serviceNodes.clear();

            for (ServiceItem serviceItem : serviceItems) {
                serviceNodes.add(new ServiceNode(this, serviceItem));
            }

            SimpleNode parent = getParent();

            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }

            updateFrom(parent);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return serviceNodes.toArray(new SimpleNode[serviceNodes.size()]);
        }

        @Override
        public String getName() {
            return MessageFormat.format("{0}({1})", this.type, this.serviceNodes.size());
        }

        @Override
        public void handleSelection(SimpleTree tree) {
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
        }
    }

    public class ModuleNode extends BaseSimpleNode {

        List<ServiceNode> serviceNodes = new ArrayList<>();

        final RestServiceProject project;

        List<ModuleNode> moduleNodes = new ArrayList<>();

        private String moduleName;

        public ModuleNode(SimpleNode parent, RestServiceProject project, String moduleName) {
            super(parent);
            this.project = project;
            this.moduleName = moduleName;
        }

        private void updateServiceNodes(List<ServiceItem> serviceItems) {
            serviceNodes.clear();

            for (ServiceItem serviceItem : serviceItems) {
                serviceNodes.add(new ServiceNode(this, serviceItem));
            }

            SimpleNode parent = getParent();

            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }

            updateFrom(parent);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return serviceNodes.toArray(new SimpleNode[serviceNodes.size()]);
        }

        @Override
        public String getName() {

            if (!moduleNodes.isEmpty()) {
                return MessageFormat.format("{0}({1})", moduleName, this.serviceNodes.size());
            }

            return moduleName;
        }
    }

    /**
     * 末端接口节点
     */
    public class ServiceNode extends BaseSimpleNode {

        ServiceItem myServiceItem;

        public ServiceNode(SimpleNode parent, ServiceItem serviceItem) {
            super(parent);
            myServiceItem = serviceItem;

            Icon icon = ToolkitIcons.METHOD.get(serviceItem.getMethod());
            if (icon != null) {
                getTemplatePresentation().setIcon(icon);
                setIcon(icon); //兼容 IDEA 2016
            }
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return new SimpleNode[0];
        }

        @Override
        public String getName() {
            return myServiceItem.getName();
        }

        @Override
        public void handleSelection(SimpleTree tree) {
            ServiceNode selectedNode = (ServiceNode) tree.getSelectedNode();
            assert selectedNode != null;
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
            try {
                ServiceNode selectedNode = (ServiceNode) tree.getSelectedNode();
                ServiceItem myServiceItem = selectedNode.myServiceItem;
                PsiElement psiElement = myServiceItem.getPsiElement();

                if (!psiElement.isValid()) {
                    throw new RuntimeException("psiElement is not valid!!!");
                    // PsiDocumentManager.getInstance(psiMethod.getProject()).commitAllDocuments();
                    // try refresh service
                    //InterfaceXNavigator.getInstance(myServiceItem.getModule().getProject()).scheduleStructureUpdate();
                }

                if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                    PsiMethod psiMethod = myServiceItem.getPsiMethod();
                    OpenSourceUtil.navigate(psiMethod);
                }
            } catch (ClassCastException ignore) {
                // ServiceNode cast ignored.
                // java.lang.ClassCastException: class jiux.net.plugin.restful.navigator.RestServiceStructure$ProjectNode
                // cannot be cast to class jiux.net.plugin.restful.navigator.RestServiceStructure$ServiceNode
                // (jiux.net.plugin.restful.navigator.RestServiceStructure$ProjectNode
                // and jiux.net.plugin.restful.navigator.RestServiceStructure$ServiceNode
                // are in unnamed module of loader com.intellij.ide.plugins.cl.PluginClassLoader @742ad77c)
            }
        }

        @Nullable
        @NonNls
        protected String getMenuId() {
            return "Toolkit.NavigatorServiceMenu";
        }

    }
}

