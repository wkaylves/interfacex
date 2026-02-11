package com.kaylves.interfacex.common.constants;

import lombok.Getter;

/**
 * @author kaylves
 */
@Getter
public enum InterfaceXItemEnum {
    /**
     * RocketMQ生产者
     */
    RocketMQProducer,

    /**
     * RocketMQ监听者
     */
    RocketMQListener,

    /**
     * RabbitMQ监听者
     */
    RabbitMQListener,

    /**
     * Rabbitmq生产者
     */
    RabbitMQProducer,

    /**
     * HTTP
     */
    HTTP,

    /**
     *OpenFeign
     */
    OpenFeign,

    /**
     * Mission
     */
    Mission;
}
