package com.kaylves.interfacex.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * 导出Excel Bean
 */
@Data
@Builder
public class ServiceExportBean implements ServiceExportBeanI{

    /**
     * 模块名称
     */
    @ExcelProperty("modelName")
    private String modelName;

    /**
     * 接口类型
     */
    @ExcelProperty("interfaceType")
    private String interfaceType;

    /**
     * 依赖服务
     */
    @ExcelProperty("dependencyService")
    private String dependencyService;

    /**
     * 路径
     */
    @ExcelProperty("path")
    private String path;

    /**
     * 描述
     */
    @ExcelProperty("pathName")
    private String pathName;

    /**
     * 是否使用
     */
    @ExcelProperty("usedAble")
    private Boolean usedAble;

    /**
     * 类名称
     */
    @ExcelProperty("fullClassName")
    private String fullClassName;

    /**
     *
     */
    @ExcelProperty("simpleClassName")
    private String simpleClassName;

    /**
     * 负责人
     */
    @ExcelProperty("auth")
    private String auth;
}
