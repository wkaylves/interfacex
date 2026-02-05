package com.kaylves.interfacex.module.mission;

import com.kaylves.interfacex.module.http.PathMappingAnnotation;

/**
 * Mission异步通知补偿
 * @author kaylves
 */
public enum MissionClientAnnotation implements PathMappingAnnotation {

    /**
     */
    MISSION_CLIENT_ANNOTATION("MissionClient", "com.hbfintech.mission.client.rest.annotation.MissionClient"),
    ;

    private final String shortName;

    private final String qualifiedName;

    MissionClientAnnotation(String shortName, String qualifiedName) {
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
