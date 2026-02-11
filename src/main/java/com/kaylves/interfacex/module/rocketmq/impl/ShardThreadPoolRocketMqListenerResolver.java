package com.kaylves.interfacex.module.rocketmq.impl;

import com.intellij.openapi.module.Module;
import com.kaylves.interfacex.module.rocketmq.RocketMQAnnotation;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShardThreadPoolRocketMqListenerResolver extends AbstractRocketMQListenerResolver {

    public ShardThreadPoolRocketMqListenerResolver(Module module) {
        this.module = module;
    }

    @Override
    public RocketMQAnnotation getRocketMQAnnotation() {
        return RocketMQAnnotation.SharedThreadPoolRocketMQMessageListener;
    }

    @Override
    public String getServiceItemCategory() {
        return "ShardRocketMqListener";
    }
}
