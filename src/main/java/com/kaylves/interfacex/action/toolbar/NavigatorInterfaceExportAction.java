package com.kaylves.interfacex.action.toolbar;

import com.alibaba.excel.EasyExcel;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.IntfxUtils;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.time.DateFormatUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * 接口导出
 * @author kaylves
 */
public class NavigatorInterfaceExportAction extends AnAction {

    public Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = getProject(e.getDataContext());
        assert project != null;

        SwingUtilities.invokeLater(() -> {
            String filename = project.getName() + "_" + DateFormatUtils.format(new Date(),"yyyyMMddHHmmss") + ".xlsx";
            File file = IdeaPluginUtils.showFileChooser(e, filename, new FileNameExtensionFilter("Excel表格(*.xlsx)", ".xlsx"));
            exportExcelFile(file, project);
        });

    }

    private void exportExcelFile(File file, Project project) {

        if (file == null) {
            return;
        }

        String path = file.getAbsolutePath();

        try {

            List<ServiceExportBean> serviceExportBeans =  ReadAction.compute(() -> IntfxUtils.getServiceExportBeans(project));

            EasyExcel.write(path, ServiceExportBean.class).sheet(project.getName()).doWrite(serviceExportBeans);


            NotificationGroupManager.getInstance()
                    // plugin.xml里配置的id
                    .getNotificationGroup("com.kaylves.plugin.interfacex.notification")
                    .createNotification("导出成功：" + path, NotificationType.IDE_UPDATE).notify(project);

            Desktop.getDesktop().open(new File(path));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
