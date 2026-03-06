package com.kaylves.interfacex.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.InterfaceProject;
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
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author kaylves
 */
@Slf4j
public class InterfaceXHelper {

    /**
     * 获取{@link InterfaceProject} 集合
     *
     * @param project 工程
     * @param navigatorState 配置项
     * @return List<InterfaceXProject>
     */
    public static List<InterfaceProject> getInterfaceProjectUsingResolver(Project project, InterfaceXNavigatorState navigatorState) {

        List<InterfaceProject> serviceProjectList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {

            Map<String, List<InterfaceItem>> restServiceItems = buildInterfaceItems(module,navigatorState);

            if (!restServiceItems.isEmpty()) {
                serviceProjectList.add(new InterfaceProject(module, restServiceItems));
            }
        }

        return serviceProjectList;
    }

    /**
     * 根据模块和导航器状态（其中包含用户配置）来获取服务解析器列表。
     * 此方法会根据状态中的配置决定启用哪些解析器。
     *
     * @param module         当前模块
     * @param navigatorState 包含用户配置的导航器状态
     * @return 根据配置启用的服务解析器列表
     */
    private static @NotNull List<IServiceResolver> obtainServiceImpls(Module module, InterfaceXNavigatorState navigatorState) {
        List<InterfaceItemConfigEntity> configEntities = navigatorState.getInterfaceItemConfigEntities();

        //未配置，默认全部
        if(configEntities==null || configEntities.isEmpty()){
            return obtainServiceImpls(module);
        }

        // 1. 将用户启用的类别转换为 Set，以便高效查找
        Set<String> enabledCategories = new HashSet<>();
        if (configEntities != null) {
            for (InterfaceItemConfigEntity entity : configEntities) {
                // 假设实体的 isEnabled() 方法表示该项是否被勾选
                if (entity.getEnabled()) {
                    enabledCategories.add(entity.getItemCategory());
                }
            }
        }

        List<IServiceResolver> serviceResolvers = new ArrayList<>();

        // 2. 根据配置决定是否添加对应的 Resolver

        // HTTP 相关
        if (enabledCategories.contains(InterfaceXItemEnum.HTTP.name())) {
            serviceResolvers.add(new SpringMVCResolverServiceResolver(module));
        }

        // HTTP 相关
        if (enabledCategories.contains(InterfaceXItemEnum.Mission.name())) {
            serviceResolvers.add(new MissionResolverServiceResolver(module));
        }

        // OpenFeign
        if (enabledCategories.contains(InterfaceXItemEnum.OpenFeign.name())) {
            serviceResolvers.add(new OpenFeignResolverServiceResolver(module));
        }

        // RocketMQ Producer (Template-based and Custom)
        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQProducer.name())) {
            serviceResolvers.add(new RocketMQTemplateProducerResolverServiceResolver(module));
            serviceResolvers.add(new RocketMQCustomProducerResolverServiceResolver(module));
        }

        // RocketMQ Listener
        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQListener.name())) {
            serviceResolvers.add(new RocketMQListenerResolverServiceResolver(module));
            serviceResolvers.add(new ShardThreadPoolRocketMqListenerResolverServiceResolver(module));
        }

        // RabbitMQ
        if (enabledCategories.contains(InterfaceXItemEnum.RabbitMQListener.name())) {
            serviceResolvers.add(new RabbitMQListenerResolverServiceResolver(module));
        }

        // RabbitMQ
        if (enabledCategories.contains(InterfaceXItemEnum.RabbitMQProducer.name())) {
            serviceResolvers.add(new RabbitMQProducerResolverServiceResolver(module));
        }

        // XXL Job
        if (enabledCategories.contains(InterfaceXItemEnum.XXLJOB.name())) {
            serviceResolvers.add(new XXLJobResolverServiceResolver(module));
        }

        return serviceResolvers;
    }


    private static @NotNull List<IServiceResolver> obtainServiceImpls(Module module) {
        List<IServiceResolver> serviceResolvers = new ArrayList<>();

        //rocketmq
        serviceResolvers.add(new RocketMQTemplateProducerResolverServiceResolver(module));
        serviceResolvers.add(new RocketMQDeliverResolverServiceResolver(module));
        serviceResolvers.add(new RocketMQCustomProducerResolverServiceResolver(module));

        //rocketmq listener
        serviceResolvers.add(new RocketMQListenerResolverServiceResolver(module));
        serviceResolvers.add(new ShardThreadPoolRocketMqListenerResolverServiceResolver(module));

        //rabbitmq
        serviceResolvers.add(new RabbitMQListenerResolverServiceResolver(module));
        serviceResolvers.add(new RabbitMQProducerResolverServiceResolver(module));

        serviceResolvers.add(new XXLJobResolverServiceResolver(module));

        //HTTP
        serviceResolvers.add(new SpringMVCResolverServiceResolver(module));
        serviceResolvers.add(new OpenFeignResolverServiceResolver(module));
        serviceResolvers.add(new MissionResolverServiceResolver(module));
        return serviceResolvers;
    }


    @NotNull
    public static List<InterfaceItem> buildInterfaceItemUsingResolver(Project project) {
        List<InterfaceItem> itemList = new ArrayList<>();

        InterfaceXNavigatorState xNavigatorState = InterfaceXNavigator.getInstance(project).getXNavigatorState();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            itemList.addAll(Objects.requireNonNull(buildInterfaceItemUsingResolver(module,xNavigatorState)));
        }

        return itemList;
    }

    public static List<InterfaceItem> buildInterfaceItemUsingResolver(Module module,InterfaceXNavigatorState xNavigatorState) {
        Map<String, List<InterfaceItem>> serviceItemMap = buildInterfaceItems(module,xNavigatorState);
        List<InterfaceItem> itemList = new ArrayList<>();
        serviceItemMap.values().forEach(itemList::addAll);
        return itemList;
    }

    public static Map<String, List<InterfaceItem>> buildInterfaceItems(Module module,InterfaceXNavigatorState navigatorState) {
        Map<String, List<InterfaceItem>> serviceItemMap = new LinkedHashMap<>();

        List<IServiceResolver> serviceResolvers = obtainServiceImpls(module,navigatorState);

        serviceResolvers.forEach(serviceResolver -> {

            long startTime = System.currentTimeMillis();

            List<InterfaceItem> interfaceItemList = serviceResolver.findServiceItemsInModule();

            long endTime = System.currentTimeMillis();

            log.info("module {} serviceItem :{} used {} ms", module.getName(), serviceResolver.getServiceItemCategory(), (endTime - startTime));

            if (!interfaceItemList.isEmpty()) {
                interfaceItemList.sort((item1, item2) -> item1.getUrl().compareTo(item2.getUrl()));
                serviceItemMap.put(serviceResolver.getServiceItemCategory(), interfaceItemList);
            }
        });
        return serviceItemMap;
    }

}
