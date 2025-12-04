package com.kaylves.interfacex.module.intfitem;

import com.kaylves.interfacex.common.annotations.rocketmq.RocketMQListenerSpringAnnotation;
import com.intellij.openapi.module.Module;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ShardThreadPoolRocketMqListenerResolver extends AbstractRocketMQListenerResolver {

    public ShardThreadPoolRocketMqListenerResolver(Module module) {
        this.module = module;
    }

    @Override
    public RocketMQListenerSpringAnnotation getRocketMQAnnotation() {
        return RocketMQListenerSpringAnnotation.SharedThreadPoolRocketMQMessageListener;
    }

    @Override
    public String getServiceItem() {
        return "ShardThreadRocketMqListener";
    }
}
