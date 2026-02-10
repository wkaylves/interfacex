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
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
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
            if (serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RabbitMQListener
                    || serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RocketMQListener
                    || serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RocketMQDeliver
                    || serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RocketMQProducer) {

                InterfaceXItem interfaceXItem = serviceNode.myInterfaceXItem;
                String requestBodyJson;
                PsiElement psiElement = interfaceXItem.getPsiElement();
                if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
                    PsiMethodHelper psiMethodHelper = PsiMethodHelper
                            .create(interfaceXItem.getPsiMethod())
                            .withModule(interfaceXItem.getModule());
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

            if (serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.XXLJob
                    || serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RocketMQDeliver
                    || serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum() == InterfaceXItemCategoryEnum.RocketMQProducer) {
                log.info("service node:{}", serviceNode.myInterfaceXItem.getName());
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
        InterfaceXNavigatorPanel interfaceXNavigatorPanel = InterfaceXNavigator.getInstance(project).getRootPannel();

        log.info("interfaceXNavigatorPanel>>>>>>>>>>>");

        InterfaceXNavigator servicesNavigator = InterfaceXNavigator.getInstance(project);

        InterfaceXItemCategoryEnum triggerInterfaceXItemCategoryEnum = InterfaceXItemCategoryEnum.getUniqueEnum(serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum());

        InterfaceXForm interfaceXForm = servicesNavigator.getFormCache().get(triggerInterfaceXItemCategoryEnum);

        if (interfaceXForm == null) {
            interfaceXForm = InterfaceXFormFactory.createInterfaceXForm(serviceNode);
            interfaceXNavigatorPanel.setBottomComponent(interfaceXForm);
            servicesNavigator.getFormCache().put(interfaceXForm.getInterfaceXEnum(), interfaceXForm);
            return;
        }

        //刷新
        interfaceXForm.flush(serviceNode.myInterfaceXItem);
        servicesNavigator.getFormCache().put(interfaceXForm.getInterfaceXEnum(), interfaceXForm);
        interfaceXNavigatorPanel.setBottomComponent(interfaceXForm);
    }
}
