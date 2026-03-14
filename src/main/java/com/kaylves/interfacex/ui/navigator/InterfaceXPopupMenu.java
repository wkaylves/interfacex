package com.kaylves.interfacex.ui.navigator;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.ide.CopyPasteManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.InterfaceXFormFactory;
import com.kaylves.interfacex.utils.PsiMethodHelper;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.datatransfer.StringSelection;

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
        // 创建右键菜单动作组
        DefaultActionGroup popupGroup = new DefaultActionGroup("MyTreePopup", true);
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

        // 将动作组绑定到 SimpleTree
        PopupHandler.installPopupMenu(simpleTree, popupGroup, "MyTreePopup");
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

    /**
     * 创建或刷新表单
     *
     * @param serviceNode serviceNode
     */
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

        //刷新
        form.flush(serviceNode.interfaceItem);
        servicesNavigator.getFormConcurrentHashMap().put(form.getInterfaceXEnum(), form);
        interfaceXNavigatorPanel.setBottomComponent(form);
    }
}
