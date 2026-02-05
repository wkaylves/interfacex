package com.kaylves.interfacex.module.spring;

import com.kaylves.interfacex.module.http.PathMappingAnnotation;

/**
 * @author kaylves
 */
public enum SpringComponentAnnotation implements PathMappingAnnotation {

    Component("Component", "org.springframework.stereotype.Component"),

    Service("Service", "org.springframework.stereotype.Service")
    ;

    private final String shortName;

    private final String qualifiedName;

    SpringComponentAnnotation(String shortName, String qualifiedName) {
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
