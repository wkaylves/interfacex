package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.rocketmq.RocketMQAnnotation;
import com.intellij.openapi.module.Module;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocketMQListenerResolver extends AbstractRocketMQListenerResolver {

    public RocketMQListenerResolver(Module module) {
        this.module = module;
    }


    @Override
    public RocketMQAnnotation getRocketMQAnnotation() {
        return RocketMQAnnotation.RocketMQMessageListener;
    }

    @Override
    public String getServiceItem() {
        return "RocketMqListener";
    }
}
