package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.form.XXLJobForm;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXPopupMenu {

    private final SimpleTree simpleTree;

    private final Project project;

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

            if(serviceNode.myServiceItem.getInterfaceXEnum()== InterfaceXEnum.XXLJob){
                log.info("service node:{}", serviceNode.myServiceItem.getName());
                createOrFlushInterfaceXForm(serviceNode);
            }
            else{
                JOptionPane.showMessageDialog(null, "仅支持XXLJob，暂不支持其它类型！", "提示", JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }

    /**
     * 创建或刷新表单
     * @param serviceNode serviceNode
     */
    private void createOrFlushInterfaceXForm(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
        InterfaceXNavigatorPanel interfaceXNavigatorPanel = InterfaceXNavigator.getInstance(project).getRootPannel();

        log.info("interfaceXNavigatorPanel>>>>>>>>>>>");

        if(interfaceXNavigatorPanel.getInterfaceXForm()==null){
            XXLJobForm xxlJobForm =  new XXLJobForm(serviceNode.myServiceItem);
            interfaceXNavigatorPanel.setBottomComponent(xxlJobForm);
            return;
        }

        log.info("flush interfaceXForm hash{}>>>>>>>>>>>", interfaceXNavigatorPanel.getInterfaceXForm().hashCode());
        //刷新
        interfaceXNavigatorPanel.getInterfaceXForm().flush(serviceNode.myServiceItem);
    }
}
