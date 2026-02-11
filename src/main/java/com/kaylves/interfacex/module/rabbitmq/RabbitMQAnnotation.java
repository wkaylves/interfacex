package com.kaylves.interfacex.module.rabbitmq;

import com.kaylves.interfacex.module.http.PathMappingAnnotation;

public enum RabbitMQAnnotation implements PathMappingAnnotation {

    PATH("RabbitListener", "org.springframework.amqp.rabbit.annotation.RabbitListener");

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
