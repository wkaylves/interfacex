package com.kaylves.interfacex.common.annotations.rocketmq;

import com.kaylves.interfacex.common.annotations.http.PathMappingAnnotation;

public enum RocketMQListenerSpringAnnotation implements PathMappingAnnotation {
    RocketMQMessageListener("RocketMQMessageListener", "org.apache.rocketmq.spring.annotation.RocketMQMessageListener"),
    SharedThreadPoolRocketMQMessageListener("SharedThreadPoolRocketMQMessageListener", "hbfintech.frame.mq.rocketmq.consumer.annotation.SharedThreadPoolRocketMQMessageListener");

    private final String shortName;
    private final String qualifiedName;

    RocketMQListenerSpringAnnotation(String shortName, String qualifiedName) {
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
