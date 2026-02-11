package com.kaylves.interfacex.module.xxljob;

import com.kaylves.interfacex.module.http.PathMappingAnnotation;

/**
 * @author kaylves
 */
public enum XXLJobComponentAnnotation implements PathMappingAnnotation {

    JobHandler("JobHandler", "com.xxl.job.core.handler.annotation.JobHandler"),

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
