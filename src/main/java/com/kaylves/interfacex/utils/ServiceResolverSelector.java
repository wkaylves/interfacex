package com.kaylves.interfacex.utils;

import com.intellij.openapi.module.Module;
import com.kaylves.interfacex.common.constants.InterfaceXItemEnum;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import com.kaylves.interfacex.module.http.SpringMVCResolverServiceResolver;
import com.kaylves.interfacex.module.mission.MissionResolverServiceResolver;
import com.kaylves.interfacex.module.openfeign.OpenFeignResolverServiceResolver;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQListenerResolverServiceResolver;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQProducerResolverServiceResolver;
import com.kaylves.interfacex.module.resolver.IServiceResolver;
import com.kaylves.interfacex.module.rocketmq.impl.*;
import com.kaylves.interfacex.module.xxljob.XXLJobResolverServiceResolver;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ServiceResolverSelector {

    private ServiceResolverSelector() {
    }

    @NotNull
    public static List<IServiceResolver> selectResolvers(Module module, InterfaceXNavigatorState navigatorState) {
        List<InterfaceItemConfigEntity> configEntities = navigatorState.getInterfaceItemConfigEntities();

        if (configEntities == null || configEntities.isEmpty()) {
            return getAllResolvers(module);
        }

        Set<String> enabledCategories = extractEnabledCategories(configEntities);
        return buildResolversByConfig(module, enabledCategories);
    }

    @NotNull
    public static List<IServiceResolver> getAllResolvers(Module module) {
        List<IServiceResolver> serviceResolvers = new ArrayList<>();

        serviceResolvers.add(new RocketMQTemplateProducerServiceResolver(module));
        serviceResolvers.add(new RocketMQDeliverResolverServiceResolver(module));
        serviceResolvers.add(new RocketMQCustomProducerResolverServiceResolver(module));
        serviceResolvers.add(new RocketMQListenerResolverServiceResolver(module));
        serviceResolvers.add(new ShardThreadPoolRocketMqListenerResolverServiceResolver(module));
        serviceResolvers.add(new RabbitMQListenerResolverServiceResolver(module));
        serviceResolvers.add(new RabbitMQProducerResolverServiceResolver(module));
        serviceResolvers.add(new XXLJobResolverServiceResolver(module));
        serviceResolvers.add(new SpringMVCResolverServiceResolver(module));
        serviceResolvers.add(new OpenFeignResolverServiceResolver(module));
        serviceResolvers.add(new MissionResolverServiceResolver(module));

        return serviceResolvers;
    }

    private static Set<String> extractEnabledCategories(List<InterfaceItemConfigEntity> configEntities) {
        Set<String> enabledCategories = new HashSet<>();
        for (InterfaceItemConfigEntity entity : configEntities) {
            if (entity.getEnabled()) {
                enabledCategories.add(entity.getItemCategory());
            }
        }
        return enabledCategories;
    }

    private static List<IServiceResolver> buildResolversByConfig(Module module, Set<String> enabledCategories) {
        List<IServiceResolver> serviceResolvers = new ArrayList<>();

        if (enabledCategories.contains(InterfaceXItemEnum.HTTP.name())) {
            serviceResolvers.add(new SpringMVCResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.Mission.name())) {
            serviceResolvers.add(new MissionResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.OpenFeign.name())) {
            serviceResolvers.add(new OpenFeignResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQProducer.name())) {
            serviceResolvers.add(new RocketMQCustomProducerResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQListener.name())) {
            serviceResolvers.add(new RocketMQListenerResolverServiceResolver(module));
            serviceResolvers.add(new ShardThreadPoolRocketMqListenerResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.RabbitMQListener.name())) {
            serviceResolvers.add(new RabbitMQListenerResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.RabbitMQTemplate.name())) {
            serviceResolvers.add(new RabbitMQProducerResolverServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQTemplate.name())) {
            serviceResolvers.add(new RocketMQTemplateProducerServiceResolver(module));
        }

        if (enabledCategories.contains(InterfaceXItemEnum.XXLJOB.name())) {
            serviceResolvers.add(new XXLJobResolverServiceResolver(module));
        }

        return serviceResolvers;
    }
}