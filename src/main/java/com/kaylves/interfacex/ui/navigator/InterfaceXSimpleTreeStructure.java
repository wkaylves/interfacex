package com.kaylves.interfacex.ui.navigator;

import com.intellij.ide.DefaultTreeExpander;
import com.intellij.ide.todo.TodoTreeBuilder;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.application.ApplicationManager;
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
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.InterfaceProject;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.ProjectInitService;
import com.intellij.icons.AllIcons;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXSimpleTreeStructure extends SimpleTreeStructure {

    RootNode myRoot = new RootNode();

    private final Map<InterfaceProject, ProjectNode> myProjectToNodeMapping = new ConcurrentHashMap<>(1);

    protected Project project;

    protected SimpleTree simpleTree;

    StructureTreeModel<?> structureTreeModel;
    
    // 标签过滤状态
    private String activeTagFilter = null;

    public InterfaceXSimpleTreeStructure(Project project, SimpleTree simpleTree) {
        this.project = project;

        this.simpleTree = simpleTree;

        structureTreeModel = new StructureTreeModel<>(this, TodoTreeBuilder.NODE_DESCRIPTOR_COMPARATOR, this.project);

        AsyncTreeModel asyncTreeModel = new AsyncTreeModel(structureTreeModel, this.project);
        simpleTree.setModel(asyncTreeModel);

        InterfaceXPopupMenu xPopupMenu = new InterfaceXPopupMenu(simpleTree,project);
        xPopupMenu.installPopupMenu();
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

        ReadAction.run(() -> {
            List<InterfaceProject> projects = ProjectInitService.getInstance(project).getServiceProjects();
            updateProjects(projects);

            updateCachedTreeState();
        });

        if (needRefresh) {
            ProgressManager.getInstance().runProcessWithProgressSynchronously(() -> ReadAction.run(() -> {
                structureTreeModel.invalidate();

                // 同步方式兼容旧版:ml-citation{ref="2" data="citationList"}
            }),"InterfaceX Scanning Implementations...",true,project);
        }
    }

    public void updateProjects(List<InterfaceProject> projects) {

        for (InterfaceProject each : projects) {
            log.info("module name : {}", each.getModuleName());
            ProjectNode node = findNodeFor(each);

            if (node == null) {
                node = new ProjectNode(myRoot, each);
                myProjectToNodeMapping.put(each, node);
            }
        }

        myRoot.childrenChanged();
        myRoot.updateProjectNodes(projects);
    }

    private ProjectNode findNodeFor(InterfaceProject project) {
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

        public void updateProjectNodes(List<InterfaceProject> projects) {
            projectNodes.clear();

            for (InterfaceProject project : projects) {
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

        final InterfaceProject myProject;

        public ProjectNode(SimpleNode parent, InterfaceProject project) {

            super(parent);
            myProject = project;

            getTemplatePresentation().setIcon(ToolkitIcons.MODULE);

            //兼容 IDEA 2016
            setIcon(ToolkitIcons.MODULE);

            updateServiceNodes(project.getServiceItemMap());
        }

        private void updateServiceNodes(Map<String, List<InterfaceItem>> serviceItems) {

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

    /**
     * 标签节点 - 按标签聚合接口
     */
    public class TagNode extends BaseSimpleNode {

        List<ServiceNode> serviceNodes = new ArrayList<>();

        final InterfaceProject project;

        private final String tagName;

        public TagNode(String tagName, SimpleNode parent, InterfaceProject project) {
            super(parent);
            this.tagName = tagName;
            this.project = project;
            
            getTemplatePresentation().setIcon(ToolkitIcons.TAG);
            setIcon(ToolkitIcons.TAG);
        }

        public void addServiceNode(ServiceNode serviceNode) {
            serviceNodes.add(serviceNode);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            return serviceNodes.toArray(new SimpleNode[serviceNodes.size()]);
        }

        @Override
        public String getName() {
            return MessageFormat.format("{0}({1})", this.tagName, this.serviceNodes.size());
        }

        @Override
        public void handleSelection(SimpleTree tree) {
        }

        @Override
        public void handleDoubleClickOrEnter(SimpleTree tree, InputEvent inputEvent) {
        }
    }

    public class CategoryNode extends BaseSimpleNode {

        List<ServiceNode> serviceNodes = new ArrayList<>();

        final InterfaceProject myProject;

        private final String type;

        public CategoryNode(String type, SimpleNode parent, InterfaceProject project) {

            super(parent);
            this.type = type;
            myProject = project;

            getTemplatePresentation().setIcon(ToolkitIcons.SERVICE);
            setIcon(ToolkitIcons.SERVICE); //兼容 IDEA 2016

            updateServiceNodes(project.getServiceItemMap().get(type));
        }

        private void updateServiceNodes(List<InterfaceItem> interfaceItems) {
            serviceNodes.clear();

            for (InterfaceItem interfaceItem : interfaceItems) {
                serviceNodes.add(new ServiceNode(this, interfaceItem));
            }

            SimpleNode parent = getParent();

            if (parent != null) {
                ((BaseSimpleNode) parent).cleanUpCache();
            }

            updateFrom(parent);
        }

        @Override
        protected SimpleNode[] buildChildren() {
            // 按标签分组接口
            try {
                StorageAdapter adapter = StorageAdapter.getInstance();
                String projectPath = InterfaceXSimpleTreeStructure.this.project.getBasePath();
                
                // 查询该 Category 下所有接口的标签
                Map<String, List<ServiceNode>> tagToServicesMap = new LinkedHashMap<>();
                List<ServiceNode> untaggedServices = new ArrayList<>();
                
                for (ServiceNode serviceNode : serviceNodes) {
                    InterfaceItem item = serviceNode.interfaceItem;
                    List<TagEntity> tags = adapter.loadTagsByInterface(
                            projectPath,
                            item.getModule() != null ? item.getModule().getName() : "",
                            item.getInterfaceItemCategoryEnum().name(),
                            item.getUrl(),
                            item.getMethod() != null ? item.getMethod().name() : null,
                            item.getPsiMethod() != null ? item.getPsiMethod().getName() : ""
                    );
                    
                    if (!tags.isEmpty()) {
                        // 取第一个标签作为分组依据
                        String firstTag = tags.get(0).getTagName();
                        tagToServicesMap.computeIfAbsent(firstTag, k -> new ArrayList<>()).add(serviceNode);
                    } else {
                        untaggedServices.add(serviceNode);
                    }
                }
                
                // 构建 TagNode 列表
                List<TagNode> tagNodes = new ArrayList<>();
                
                // 如果有活跃的标签过滤，只显示匹配的标签
                String activeFilter = InterfaceXSimpleTreeStructure.this.getActiveTagFilter();
                if (activeFilter != null && !activeFilter.isEmpty()) {
                    // 只显示匹配的标签节点
                    if (tagToServicesMap.containsKey(activeFilter)) {
                        TagNode tagNode = new TagNode(activeFilter, this, myProject);
                        for (ServiceNode serviceNode : tagToServicesMap.get(activeFilter)) {
                            tagNode.addServiceNode(serviceNode);
                        }
                        tagNodes.add(tagNode);
                    }
                    // 如果过滤标签不存在于当前 Category，返回空数组
                } else {
                    // 没有过滤，显示所有标签
                    // 有标签的节点
                    for (Map.Entry<String, List<ServiceNode>> entry : tagToServicesMap.entrySet()) {
                        TagNode tagNode = new TagNode(entry.getKey(), this, myProject);
                        for (ServiceNode serviceNode : entry.getValue()) {
                            tagNode.addServiceNode(serviceNode);
                        }
                        tagNodes.add(tagNode);
                    }
                    
                    // 未分类节点
                    if (!untaggedServices.isEmpty()) {
                        TagNode untaggedNode = new TagNode("默认", this, myProject);
                        for (ServiceNode serviceNode : untaggedServices) {
                            untaggedNode.addServiceNode(serviceNode);
                        }
                        tagNodes.add(untaggedNode);
                    }
                }
                
                return tagNodes.toArray(new SimpleNode[tagNodes.size()]);
            } catch (Exception e) {
                log.error("Failed to build tag nodes", e);
                // 降级:直接返回 ServiceNode
                return serviceNodes.toArray(new SimpleNode[serviceNodes.size()]);
            }
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

        final InterfaceProject project;

        List<ModuleNode> moduleNodes = new ArrayList<>();

        private String moduleName;

        public ModuleNode(SimpleNode parent, InterfaceProject project, String moduleName) {
            super(parent);
            this.project = project;
            this.moduleName = moduleName;
        }

        private void updateServiceNodes(List<InterfaceItem> interfaceItems) {
            serviceNodes.clear();

            for (InterfaceItem interfaceItem : interfaceItems) {
                serviceNodes.add(new ServiceNode(this, interfaceItem));
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

        public InterfaceItem interfaceItem;

        public ServiceNode(SimpleNode parent, InterfaceItem interfaceItem) {
            super(parent);
            this.interfaceItem = interfaceItem;

            Icon icon = ToolkitIcons.METHOD.get(interfaceItem.getMethod(), interfaceItem.getUsable());
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
            return interfaceItem.getName();
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
                assert selectedNode != null;
                InterfaceItem myInterfaceItem = selectedNode.interfaceItem;
                PsiElement psiElement = myInterfaceItem.getPsiElement();

                // 如果 psiElement 为 null(从缓存加载),尝试智能查找
                if (psiElement == null) {
                    log.debug("PsiElement is null, trying smart lookup by className + methodName");
                    
                    String className = myInterfaceItem.getCachedClassName();
                    String methodName = myInterfaceItem.getCachedMethodName();
                    
                    log.debug("Cached data - className: {}, methodName: {}, module: {}", 
                            className, methodName, 
                            myInterfaceItem.getModule() != null ? myInterfaceItem.getModule().getName() : "null");
                    
                    if (className != null && methodName != null && myInterfaceItem.getModule() != null) {
                        PsiMethod foundMethod = findPsiMethodByClassNameAndMethodName(
                                className, 
                                methodName, 
                                myInterfaceItem.getModule()
                        );
                        
                        if (foundMethod != null) {
                            log.info("Found method via smart lookup: {}.{}", className, methodName);
                            OpenSourceUtil.navigate(foundMethod);
                            return;
                        } else {
                            log.warn("Method not found: {}.{}", className, methodName);
                        }
                    }
                    
                    // 如果智能查找失败,提示用户刷新
                    JOptionPane.showMessageDialog(
                            tree,
                            "无法找到该方法,请先刷新项目以获取最新代码位置\n" +
                            "类名: " + (className != null ? className : "未知") + "\n" +
                            "方法: " + (methodName != null ? methodName : "未知"),
                            "提示",
                            JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }

                if (!psiElement.isValid()) {
                    throw new RuntimeException("psiElement is not valid!!!");
                    // PsiDocumentManager.getInstance(psiMethod.getProject()).commitAllDocuments();
                    // try refresh service
                    //InterfaceXNavigator.getInstance(myServiceItem.getModule().getProject()).scheduleStructureUpdate();
                }

                if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                    PsiMethod psiMethod = myInterfaceItem.getPsiMethod();
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


    private void updateCachedTreeState() {
        if (!ApplicationManager.getApplication().isDispatchThread()) {
            ApplicationManager.getApplication().invokeLater(this::updateCachedTreeState);
            return;
        }

        if (simpleTree == null || simpleTree.getRowCount() == 0) {
            return; // 树为空，跳过
        }
    }
    
    /**
     * 根据 className 和 methodName 智能查找 PsiMethod
     */
    private static PsiMethod findPsiMethodByClassNameAndMethodName(String className, String methodName, com.intellij.openapi.module.Module module) {
        try {
            com.intellij.psi.JavaPsiFacade javaPsiFacade = 
                com.intellij.psi.JavaPsiFacade.getInstance(module.getProject());
            
            log.info("Attempting to find method: {}.{} in module: {}", className, methodName, module.getName());
            
            // 策略1: 使用模块范围查找(包含依赖)
            com.intellij.psi.search.GlobalSearchScope moduleScope = 
                com.intellij.psi.search.GlobalSearchScope.moduleWithDependenciesAndLibrariesScope(module);
            com.intellij.psi.PsiClass psiClass = javaPsiFacade.findClass(className, moduleScope);
            
            // 策略2: 如果模块范围找不到,尝试项目全量范围
            if (psiClass == null) {
                log.debug("Class not found in module scope, trying project scope: {}", className);
                com.intellij.psi.search.GlobalSearchScope projectScope = 
                    com.intellij.psi.search.GlobalSearchScope.allScope(module.getProject());
                psiClass = javaPsiFacade.findClass(className, projectScope);
            }
            
            if (psiClass == null) {
                log.warn("Class not found in any scope: {}", className);
                return null;
            }
            
            log.debug("Found class: {}, searching for method: {}", psiClass.getQualifiedName(), methodName);
            
            // 查找方法(包括重载)
            com.intellij.psi.PsiMethod[] methods = psiClass.findMethodsByName(methodName, false);
            if (methods.length == 0) {
                log.warn("Method not found: {} in class {}", methodName, className);
                return null;
            }
            
            log.info("Successfully found {} methods with name: {}, using first one", methods.length, methodName);
            
            // 如果有多个重载方法,返回第一个
            // TODO: 可以根据参数类型进一步精确匹配
            return methods[0];
            
        } catch (Exception e) {
            log.error("Failed to find method: {}.{}", className, methodName, e);
            return null;
        }
    }
    
    /**
     * 应用标签过滤
     */
    public void applyTagFilter(String tagName) {
        this.activeTagFilter = tagName;
        log.info("Applying tag filter: {}", tagName);
        
        // 刷新树结构
        if (structureTreeModel != null) {
            structureTreeModel.invalidate();
        }
    }
    
    /**
     * 清除标签过滤
     */
    public void clearTagFilter() {
        this.activeTagFilter = null;
        log.info("Clearing tag filter");
        
        // 刷新树结构
        if (structureTreeModel != null) {
            structureTreeModel.invalidate();
        }
    }
    
    /**
     * 获取当前活动的标签过滤
     */
    public String getActiveTagFilter() {
        return activeTagFilter;
    }
}

