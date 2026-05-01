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

        // 标签操作 - 统一入口
        popupGroup.addAction(new AnAction("标签...") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                openTagDialog();
            }
        });
        
        popupGroup.addSeparator();
        
        popupGroup.addAction(new AnAction("清除过滤") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                clearFilterAction();
            }
        });

        PopupHandler.installPopupMenu(simpleTree, popupGroup, "MyTreePopup");
    }

    private void openTagDialog() {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        InterfaceItem currentItem = null;
        
        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            currentItem = serviceNode.interfaceItem;
        }
        
        TagOperationDialog dialog = new TagOperationDialog(project, simpleTree, currentItem);
        dialog.showDialog();
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
