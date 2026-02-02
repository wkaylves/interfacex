package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.XXLJobForm;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXPopupMenu {

    private SimpleTree simpleTree;

    private Project project;

    public InterfaceXPopupMenu(SimpleTree simpleTree, Project project) {
        this.simpleTree = simpleTree;
        this.project = project;
    }

    public void installPopupMenu(){
        // 创建右键菜单动作组
        DefaultActionGroup popupGroup = new DefaultActionGroup("MyTreePopup", true);
        popupGroup.addAction(new AnAction("执行") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                log.info("设置 exe >>>>>>>>>>>>>>>>>>>>>>>>>>>");
                settlementAction(e);
            }
        });

        // 将动作组绑定到 SimpleTree
        PopupHandler.installPopupMenu(simpleTree, popupGroup, "MyTreePopup");
    }

    public void settlementAction(AnActionEvent e) {
        SimpleNode simpleNode = simpleTree.getSelectedNode();
        if (simpleNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            log.info("service node>>>>>>>>>");

            log.info("service node:{}", serviceNode.myServiceItem.getName());
            showXXLJobForm(serviceNode);
        }
    }

    private void showXXLJobForm(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
        XXLJobForm xxlJobForm =  new XXLJobForm(serviceNode.myServiceItem);
        InterfaceXNavigator interfaceXNavigator = InterfaceXNavigator.getInstance(project);
        interfaceXNavigator.getRootPannel().setBottomComponent(xxlJobForm.getRootPanel());
    }
}
