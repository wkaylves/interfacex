package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigEntity {
    private Long id;
    private String projectPath;
    private String configKey;
    private String configValue;
    private Long updatedTime;
}
