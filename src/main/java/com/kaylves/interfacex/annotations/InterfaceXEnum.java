package com.kaylves.interfacex.annotations;

import com.kaylves.interfacex.strategy.*;
import lombok.Getter;

@Getter
public enum InterfaceXEnum {

    RocketMQProducer(RocketProducerServiceStrategy.class),
    RocketMQDeliver(RocketMQDeliverServiceStrategy.class),
    RocketMQListener(RocketMQListenerStrategy.class),

    RabbitMQListener(RabbitMQStrategy.class),
    RabbitMQProducer(RabbitMQProducerServiceStrategy.class),

    XXLJob(XXLJobServiceStrategy.class),
    HTTP(SpringControllerStrategy.class),
    OpenFeign(OpenFeignStrategy.class),
    Mission(MissionStrategy.class);

    private final Class<?> strategy;

    InterfaceXEnum(Class<?> strategy) {
        this.strategy = strategy;
    }

}
