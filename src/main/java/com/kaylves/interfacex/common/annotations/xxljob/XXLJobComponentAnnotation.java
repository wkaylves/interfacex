package com.kaylves.interfacex.common.annotations.xxljob;

import com.kaylves.interfacex.common.annotations.http.PathMappingAnnotation;

/**
 * @author kaylves
 */
public enum XXLJobComponentAnnotation implements PathMappingAnnotation {

    /**
     * 类上注解
     */
    JobHandler("JobHandler", "com.xxl.job.core.handler.annotation.JobHandler"),

    /**
     * 方法上注解
     */
    XxlJob("XxlJob", "com.xxl.job.core.handler.annotation.XxlJob"),
    ;

    private final String shortName;

    private final String qualifiedName;

    XXLJobComponentAnnotation(String shortName, String qualifiedName) {
        this.shortName = shortName;
        this.qualifiedName = qualifiedName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public String getShortName() {
        return shortName;
    }

}
