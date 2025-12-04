package com.kaylves.interfacex.common.annotations.http;

public interface PathMappingAnnotation {
    /**
     * 类全称
     * @return 类全称
     */
    String getQualifiedName();

    /**
     * 类简称
     * @return 类简称
     */
    String getShortName();
}
