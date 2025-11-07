package com.kaylves.interfacex.annotations.rocketmq;

import com.kaylves.interfacex.annotations.http.PathMappingAnnotation;

public enum RocketMQProducerAnnotation implements PathMappingAnnotation {

    ClassAnnotation("ProducerClients", "com.hbfintech.frame.mq.api.annotation.ProducerClients"),

    CommonMessageProducerClient("CommonMessageProducerClient", "com.hbfintech.frame.mq.api.annotation.CommonMessageProducerClient"),

    AsyncMessageProducerClient("AsyncMessageProducerClient", "com.hbfintech.frame.mq.api.annotation.AsyncMessageProducerClient"),

    DelayMessageClient("DelayMessageClient", "com.hbfintech.frame.mq.api.annotation.DelayMessageClient");

    private final String shortName;
    private final String qualifiedName;

    RocketMQProducerAnnotation(String shortName, String qualifiedName) {
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

    public static RocketMQProducerAnnotation[] getPathArray() {
        return new RocketMQProducerAnnotation[]{CommonMessageProducerClient,AsyncMessageProducerClient,DelayMessageClient};
    }
}
