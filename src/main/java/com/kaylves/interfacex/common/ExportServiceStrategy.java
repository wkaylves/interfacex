package com.kaylves.interfacex.common;

import com.intellij.openapi.project.Project;

import java.util.List;

/**
 * 导出Excel各类型策略基类
 * @author kaylves
 */
public interface ExportServiceStrategy<T> {

    /**
     * 获取服务导出Beans
     */
    List<T>     obtainServiceExportBeans(Project project);
}
