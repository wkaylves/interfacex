package com.kaylves.interfacex.db.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResultEntity {
    private Long id;
    private String projectPath;
    private String moduleName;
    private String category;
    private String url;
    private String httpMethod;
    private String className;
    private String methodName;
    private Integer psiElementHash;
    private String partner;
    private Long scanTime;
}
