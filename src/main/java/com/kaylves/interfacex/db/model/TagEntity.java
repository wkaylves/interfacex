package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TagEntity {
    private Long id;
    private String projectPath;
    private String moduleName;
    private String category;
    private String url;
    private String httpMethod;
    private String methodName;
    private String tagName;
    private String tagValue;
    private Long createdTime;
    private Long updatedTime;
}
