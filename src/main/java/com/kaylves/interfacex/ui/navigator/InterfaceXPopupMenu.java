package com.kaylves.interfacex.ui.navigator;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.InterfaceXFormFactory;
import com.kaylves.interfacex.utils.PsiMethodHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

/**
 * @author kaylves
 * @since 1.2.0
 */
@Slf4j
public class InterfaceXPopupMenu {

    private final SimpleTree simpleTree;

    private final Project project;

    public InterfaceXPopupMenu(SimpleTree simpleTree, Project project) {
        this.simpleTree = simpleTree;
        this.project = project;
    }

    public void installPopupMenu() {
        DefaultActionGroup popupGroup = new DefaultActionGroup("MyTreePopup", true);
        
        // ServiceNode 的菜单
        popupGroup.addAction(new AnAction("执行") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                log.info("设置 exe >>>>>>>>>>>>>>>>>>>>>>>>>>>");
                settlementAction(e);
            }
        });

        popupGroup.addAction(new AnAction("Copy Param Json") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copyParamJson();
            }
        });

        popupGroup.addAction(new AnAction("复制URL") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                copyUrl();
            }
        });

        popupGroup.addSeparator();

        // 标签操作子菜单
        DefaultActionGroup tagGroup = new DefaultActionGroup("标签", true);
        tagGroup.getTemplatePresentation().setIcon(ToolkitIcons.TAG);
        
        tagGroup.addAction(new AnAction("添加标签...") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                quickAddTag();
            }
        });
        
        tagGroup.addAction(new AnAction("移除标签...") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                quickRemoveTag();
            }
        });
        
        tagGroup.addSeparator();
        
        tagGroup.addAction(new AnAction("标签管理...") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                manageTagAction();
            }
        });
        
        popupGroup.add(tagGroup);
        
        popupGroup.addSeparator();
        
        popupGroup.addAction(new AnAction("清除过滤") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                clearFilterAction();
            }
        });

        PopupHandler.installPopupMenu(simpleTree, popupGroup, "MyTreePopup");
    }

    private void quickAddTag() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        if (!(simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode)) {
            return;
        }
        
        InterfaceItem item = serviceNode.interfaceItem;
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        List<TagEntity> allTags = adapter.loadTags(projectPath);
        List<String> existingTags = allTags.stream()
                .map(TagEntity::getTagName)
                .distinct()
                .toList();
        
        if (existingTags.isEmpty()) {
            String tagName = JOptionPane.showInputDialog(null, "请输入新标签名称:", "添加标签", JOptionPane.PLAIN_MESSAGE);
            if (tagName == null || tagName.trim().isEmpty()) {
                return;
            }
            saveTag(item, projectPath, tagName.trim());
        } else {
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            JComboBox<String> tagComboBox = new JComboBox<>(existingTags.toArray(new String[0]));
            tagComboBox.setEditable(true);
            tagComboBox.setSelectedIndex(-1);
            
            panel.add(new JLabel("选择或输入标签:"), BorderLayout.NORTH);
            panel.add(tagComboBox, BorderLayout.CENTER);
            
            int result = JOptionPane.showConfirmDialog(null, panel, "添加标签", 
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            
            if (result != JOptionPane.OK_OPTION) {
                return;
            }
            
            String tagName = (String) tagComboBox.getSelectedItem();
            if (tagName == null || tagName.trim().isEmpty()) {
                return;
            }
            
            saveTag(item, projectPath, tagName.trim());
        }
    }
    
    private void saveTag(InterfaceItem item, String projectPath, String tagName) {
        StorageAdapter adapter = StorageAdapter.getInstance();
        
        TagEntity tagEntity = TagEntity.builder()
            .projectPath(projectPath)
            .moduleName(item.getModule() != null ? item.getModule().getName() : "")
            .category(item.getInterfaceItemCategoryEnum().name())
            .url(item.getUrl())
            .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
            .methodName(item.getPsiMethod() != null ? item.getPsiMethod().getName() : "")
            .tagName(tagName)
            .createdTime(System.currentTimeMillis())
            .updatedTime(System.currentTimeMillis())
            .build();
        
        adapter.saveTag(tagEntity);
        JOptionPane.showMessageDialog(null, "标签添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    private void manageTagAction() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        InterfaceItem currentItem = null;
        
        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            currentItem = serviceNode.interfaceItem;
        }
        
        // 打开统一标签管理器
        UnifiedTagManagerDialog dialog = new UnifiedTagManagerDialog(project, simpleTree, currentItem);
        dialog.setVisible(true);
    }

    private void quickRemoveTag() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        if (!(simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode)) {
            return;
        }
        
        InterfaceItem item = serviceNode.interfaceItem;
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        List<TagEntity> tags = adapter.loadTagsByInterface(
            projectPath,
            item.getModule() != null ? item.getModule().getName() : "",
            item.getInterfaceItemCategoryEnum().name(),
            item.getUrl(),
            item.getMethod() != null ? item.getMethod().name() : null,
            item.getPsiMethod() != null ? item.getPsiMethod().getName() : ""
        );
        
        if (tags.isEmpty()) {
            JOptionPane.showMessageDialog(null, "当前接口没有标签", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String[] tagNames = tags.stream().map(TagEntity::getTagName).toArray(String[]::new);
        String selected = (String) JOptionPane.showInputDialog(
            null, "选择要移除的标签:", "移除标签",
            JOptionPane.PLAIN_MESSAGE, null, tagNames, tagNames[0]
        );
        
        if (selected == null) {
            return;
        }
        
        adapter.deleteTag(
            projectPath,
            item.getModule() != null ? item.getModule().getName() : "",
            item.getInterfaceItemCategoryEnum().name(),
            item.getUrl(),
            item.getMethod() != null ? item.getMethod().name() : null,
            item.getPsiMethod() != null ? item.getPsiMethod().getName() : "",
            selected
        );
        
        JOptionPane.showMessageDialog(null, "标签移除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }


    private void clearFilterAction() {
        InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);
        if (navigator != null) {
            navigator.clearTagFilter();
            JOptionPane.showMessageDialog(null, "过滤已清除", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void copyParamJson() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();

        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            if (serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RabbitMQListener
                    || serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RocketMQListener
                    || serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RocketMQDeliver
                    || serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RocketMQProducer) {

                InterfaceItem interfaceItem = serviceNode.interfaceItem;
                String requestBodyJson;
                PsiElement psiElement = interfaceItem.getPsiElement();
                if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                    PsiMethodHelper psiMethodHelper = PsiMethodHelper
                            .create(interfaceItem.getPsiMethod())
                            .withModule(interfaceItem.getModule());
                    requestBodyJson = psiMethodHelper.buildRequestBodyJson();
                    log.info("requestBodyJson:{}", requestBodyJson);
                    CopyPasteManager.getInstance().setContents(new StringSelection(requestBodyJson));
                }
            }
        }
    }

    private void copyUrl() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();

        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            InterfaceItem interfaceItem = serviceNode.interfaceItem;
            String url = interfaceItem.getUrl();
            
            if (url != null && !url.isEmpty()) {
                CopyPasteManager.getInstance().setContents(new StringSelection(url));
                log.info("Copied URL to clipboard: {}", url);
                JOptionPane.showMessageDialog(null, "URL已复制到剪贴板：\n" + url, "提示", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(null, "该节点没有可用的URL", "提示", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    public void settlementAction(AnActionEvent e) {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {

            if (serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.XXLJob
                    || serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RocketMQDeliver
                    || serviceNode.interfaceItem.getInterfaceItemCategoryEnum() == InterfaceItemCategoryEnum.RocketMQProducer) {
                log.info("service node:{}", serviceNode.interfaceItem.getName());
                createOrFlushInterfaceXForm(serviceNode);
            } else {
                JOptionPane.showMessageDialog(null, "当前类型暂不支持！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    private void createOrFlushInterfaceXForm(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
        InterfaceXNavigatorPanel interfaceXNavigatorPanel = InterfaceXNavigator.getInstance(project).getRootPanel();

        log.info("interfaceXNavigatorPanel>>>>>>>>>>>");

        InterfaceXNavigator servicesNavigator = InterfaceXNavigator.getInstance(project);

        InterfaceItemCategoryEnum triggerInterfaceItemCategoryEnum = InterfaceItemCategoryEnum.getUniqueEnum(serviceNode.interfaceItem.getInterfaceItemCategoryEnum());

        InterfaceXForm form = servicesNavigator.getFormConcurrentHashMap().get(triggerInterfaceItemCategoryEnum);

        if (form == null) {
            form = InterfaceXFormFactory.createInterfaceForm(serviceNode);
            interfaceXNavigatorPanel.setBottomComponent(form);
            servicesNavigator.getFormConcurrentHashMap().put(form.getInterfaceXEnum(), form);
            return;
        }

        form.flush(serviceNode.interfaceItem);
        servicesNavigator.getFormConcurrentHashMap().put(form.getInterfaceXEnum(), form);
        interfaceXNavigatorPanel.setBottomComponent(form);
    }
}
