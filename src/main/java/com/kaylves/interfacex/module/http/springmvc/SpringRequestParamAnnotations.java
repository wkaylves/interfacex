package com.kaylves.interfacex.module.http;

public enum SpringRequestParamAnnotations implements PathMappingAnnotation {
    /**
     * org.springframework.web.bind.annotation.RequestParam
     */
    REQUEST_PARAM("RequestParam", "org.springframework.web.bind.annotation.RequestParam"),
    /**
     * org.springframework.web.bind.annotation.RequestBody
     */
    REQUEST_BODY("RequestBody", "org.springframework.web.bind.annotation.RequestBody"),
    /**
     * org.springframework.web.bind.annotation.PathVariable
     */
    PATH_VARIABLE("PathVariable", "org.springframework.web.bind.annotation.PathVariable");

    private final String shortName;
    private final String qualifiedName;

    SpringRequestParamAnnotations(String shortName, String qualifiedName) {
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
