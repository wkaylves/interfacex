package com.kaylves.interfacex.ui.navigator;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.PopupHandler;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.ui.form.XXLJobForm;
import com.xxl.job.core.biz.ExecutorBiz;
import com.xxl.job.core.biz.client.ExecutorBizClient;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.biz.model.TriggerParam;
import com.xxl.job.core.enums.ExecutorBlockStrategyEnum;
import com.xxl.job.core.glue.GlueTypeEnum;
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
            showXXLJobDialog(serviceNode);
        }
    }

    public void exeXXLJOB(XXLJobForm xxlJobForm) {
        String addressUrl = xxlJobForm.getAddressText().getText();
        String accessToken=null;

        ExecutorBiz executorBiz = new ExecutorBizClient(addressUrl, accessToken);

        // trigger data
        final TriggerParam triggerParam = new TriggerParam();
        triggerParam.setJobId(1);
        triggerParam.setExecutorHandler(xxlJobForm.getJobName());
        triggerParam.setExecutorParams(null);
        triggerParam.setExecutorBlockStrategy(ExecutorBlockStrategyEnum.COVER_EARLY.name());
        triggerParam.setGlueType(GlueTypeEnum.BEAN.name());
        triggerParam.setGlueSource(null);
        triggerParam.setGlueUpdatetime(System.currentTimeMillis());
        triggerParam.setLogId(1);
        triggerParam.setLogDateTime(System.currentTimeMillis());

        // Act
        final ReturnT<String> retval = executorBiz.run(triggerParam);
        Gson gson = new Gson();
        String resultJson = gson.toJson(retval);
        xxlJobForm.getResutTextArea().setText(resultJson);
    }



    private void showXXLJobDialog(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
        XXLJobForm xxlJobForm =  new XXLJobForm(serviceNode.getName());
        // 创建GUI对象
        // 构建对话框
        DialogBuilder dialogBuilder = new DialogBuilder(project);
        // 设置对话框显示内容
        dialogBuilder.setCenterPanel(xxlJobForm.getRootPanel());
        dialogBuilder.setTitle("XXL-JOB执行");
        dialogBuilder.setOkOperation(() -> exeXXLJOB(xxlJobForm));
        // 显示对话框
        dialogBuilder.show();
    }
}
