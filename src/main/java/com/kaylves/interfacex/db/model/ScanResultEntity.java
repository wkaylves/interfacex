package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 扫描结果实体，对应 scan_result 表
 * <p>记录每次接口扫描的结果，包含接口的模块、分类、URL、HTTP方法等元信息</p>
 * <p>联合唯一约束: (project_path, module_name, category, url, http_method, method_name)</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultEntity {
    /** 自增主键 */
    private Long id;
    /** 项目根路径，用于区分不同项目的扫描结果 */
    private String projectPath;
    /** 模块名称 */
    private String moduleName;
    /** 接口分类（如 Controller、FeignClient 等） */
    private String category;
    /** 接口请求 URL */
    private String url;
    /** HTTP 请求方法（GET/POST/PUT/DELETE 等） */
    private String httpMethod;
    /** 所在类的全限定名 */
    private String className;
    /** 方法名称 */
    private String methodName;
    /** PsiElement 哈希值，用于判断代码是否变更 */
    private Integer psiElementHash;
    /** 协作者信息 */
    private String partner;
    /** 扫描时间戳 */
    private Long scanTime;
}
