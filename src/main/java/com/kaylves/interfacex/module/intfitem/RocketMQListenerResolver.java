package com.kaylves.interfacex.module.intfitem;

import com.kaylves.interfacex.common.annotations.InterfaceXEnum;
import com.kaylves.interfacex.common.annotations.rocketmq.RocketMQListenerSpringAnnotation;
import com.intellij.openapi.module.Module;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RocketMQListenerResolver extends AbstractRocketMQListenerResolver {

    public RocketMQListenerResolver(Module module) {
        this.module = module;
    }


    @Override
    public RocketMQListenerSpringAnnotation getRocketMQAnnotation() {
        return RocketMQListenerSpringAnnotation.RocketMQMessageListener;
    }

    @Override
    public String getServiceItem() {
        return InterfaceXEnum.RocketMQListener.name();
    }
}
