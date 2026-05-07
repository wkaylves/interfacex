package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 配置项实体，对应 config 表
 * <p>联合主键: (project_path, config_key)，无自增 id</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {
    /** 项目根路径，联合主键之一 */
    private String projectPath;
    /** 配置键名，联合主键之一 */
    private String configKey;
    /** 配置值 */
    private String configValue;
    /** 最后更新时间戳 */
    private Long updatedTime;
}
