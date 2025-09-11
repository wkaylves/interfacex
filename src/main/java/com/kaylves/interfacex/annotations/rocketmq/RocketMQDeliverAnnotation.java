package com.kaylves.interfacex.annotations.rocketmq;

import com.kaylves.interfacex.annotations.http.PathMappingAnnotation;

public enum RocketMQDeliverAnnotation implements PathMappingAnnotation {

    PATH("Deliver", "com.hbfintech.pigeonV2.client.core.annotation.Deliver");

    private final String shortName;
    private final String qualifiedName;

    RocketMQDeliverAnnotation(String shortName, String qualifiedName) {
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
