package com.kaylves.interfacex.bean;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * 国际化文件导出Bean
 */
@Data
public class ModulePropertiesExportBean {

    /**
     * modelName
     */
    @ExcelProperty("modelName")
    private String modelName;

    /**
     * key
     */
    @ExcelProperty("key")
    private String key;

    /**
     * value
     */
    @ExcelProperty("value")
    private String value;
}

