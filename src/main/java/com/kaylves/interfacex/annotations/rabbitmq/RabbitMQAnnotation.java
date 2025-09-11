package com.kaylves.interfacex.annotations.rabbitmq;

import com.kaylves.interfacex.annotations.http.PathMappingAnnotation;

public enum RabbitMQAnnotation implements PathMappingAnnotation {

    PATH("RabbitListener", "org.springframework.amqp.rabbit.annotation.RabbitListener"),
    SharedThreadPoolListener("SharedThreadPoolRocketMQMessageListener", "hbfintech.frame.mq.rocketmq.consumer.annotation.SharedThreadPoolRocketMQMessageListener");

    private final String shortName;
    private final String qualifiedName;

    RabbitMQAnnotation(String shortName, String qualifiedName) {
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
