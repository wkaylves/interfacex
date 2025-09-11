package com.kaylves.interfacex.annotations.http;

public enum SpringHttpRequestAnnotation implements PathMappingAnnotation {

    /**
     * org.springframework.cloud.openfeign.FeignClient
     */
    FEIGN_CLIENT("FeignClient", "org.springframework.cloud.openfeign.FeignClient"),
    ;

    private final String shortName;

    private final String qualifiedName;

    SpringHttpRequestAnnotation(String shortName, String qualifiedName) {
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
