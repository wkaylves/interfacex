package com.kaylves.interfacex.action.toolbar;

import com.alibaba.excel.EasyExcel;
import com.kaylves.interfacex.bean.ModulePropertiesExportBean;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.IntfxUtils;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateFormatUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * 国际化错误码导出
 * @author kaylves
 */
@Slf4j
public class ProjectInternationalizationExportAction extends AnAction {

    private static final Logger LOG = Logger.getInstance(RefreshProjectAction.class);

    public Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = getProject(e.getDataContext());

        log.info("action trigger>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

        if (project == null) {
            log.info("project is null");
            return;
        }

        SwingUtilities.invokeLater(() -> {
            String filename = project.getName() + "_国际化文件_" + DateFormatUtils.format(new Date(),"yyyyMMddHHmmss") + ".xlsx";
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

            List<ModulePropertiesExportBean> modulePropertiesExportBeans =  ReadAction.compute(() -> IntfxUtils.getModulePropertiesExportBeans(project));

            EasyExcel.write(path, ModulePropertiesExportBean.class).sheet(project.getName()).doWrite(modulePropertiesExportBeans);

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
