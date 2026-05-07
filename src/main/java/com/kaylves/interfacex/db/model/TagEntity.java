package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签实体，对应 tag 表
 * <p>用于给接口打标签，支持按标签过滤和管理接口</p>
 * <p>联合唯一约束: (project_path, module_name, category, url, http_method, method_name, tag_name)</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {
    /** 自增主键 */
    private Long id;
    /** 项目根路径 */
    private String projectPath;
    /** 模块名称 */
    private String moduleName;
    /** 接口分类 */
    private String category;
    /** 接口请求 URL */
    private String url;
    /** HTTP 请求方法 */
    private String httpMethod;
    /** 方法名称 */
    private String methodName;
    /** 标签名称 */
    private String tagName;
    /** 标签值（可选） */
    private String tagValue;
    /** 创建时间戳 */
    private Long createdTime;
    /** 更新时间戳 */
    private Long updatedTime;
}
