package com.kaylves.interfacex.module.rocketmq.impl;

import com.intellij.openapi.module.Module;
import com.kaylves.interfacex.module.rocketmq.RocketMQAnnotation;
import lombok.extern.slf4j.Slf4j;

/**
 * @author kaylves
 */
@Slf4j
public class ShardThreadPoolRocketMqListenerResolverServiceResolver extends AbstractRocketMQListenerResolverServiceResolver {

    public ShardThreadPoolRocketMqListenerResolverServiceResolver(Module module) {
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
