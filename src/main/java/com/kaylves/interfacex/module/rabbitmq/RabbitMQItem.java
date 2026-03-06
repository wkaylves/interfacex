package com.kaylves.interfacex.module.rabbitmq;

import com.kaylves.interfacex.common.InterfaceUrl;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RabbitMQItem implements InterfaceUrl {

    private String exchangeName;

    private String routingKey;

    private String queueName;

    @Override
    public String getUrl() {
        return this.queueName;
    }
}
