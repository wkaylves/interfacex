package com.kaylves.interfacex.annotations.mission;

import com.kaylves.interfacex.annotations.http.PathMappingAnnotation;

/**
 *
 */
public enum MissionClientMethodAnnotation implements PathMappingAnnotation {
    /**
     * RequestMapping
     */
    MISSION_CLIENT_METHOD_ANNOTATION("com.hbfintech.mission.client.rest.annotation.MissionClientMethod", null)
    ;

    private final String qualifiedName;
    private final String methodName;

    MissionClientMethodAnnotation(String qualifiedName, String methodName) {
        this.qualifiedName = qualifiedName;
        this.methodName = methodName;
    }

    public static MissionClientMethodAnnotation getByQualifiedName(String qualifiedName) {
        for (MissionClientMethodAnnotation springRequestAnnotation : MissionClientMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().equals(qualifiedName)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public static MissionClientMethodAnnotation getByShortName(String requestMapping) {
        for (MissionClientMethodAnnotation springRequestAnnotation : MissionClientMethodAnnotation.values()) {
            if (springRequestAnnotation.getQualifiedName().endsWith(requestMapping)) {
                return springRequestAnnotation;
            }
        }
        return null;
    }

    public String methodName() {
        return this.methodName;
    }

    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    @Override
    public String getShortName() {
        return qualifiedName.substring(qualifiedName.lastIndexOf(".") - 1);
    }
}
