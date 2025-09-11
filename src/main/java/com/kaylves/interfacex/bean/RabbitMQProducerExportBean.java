package com.kaylves.interfacex.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RabbitMQProducerExportBean implements ServiceExportBeanI{

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


    @ExcelProperty("methodName")
    private String methodName;

    @ExcelProperty("lineNumber")
    private Integer lineNumber;

    /**
     * 描述
     */
    @ExcelProperty("desc")
    private String desc;

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
