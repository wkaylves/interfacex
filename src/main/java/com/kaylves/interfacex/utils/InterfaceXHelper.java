package com.kaylves.interfacex.utils;

import com.kaylves.interfacex.module.http.SpringMVCResolver;
import com.kaylves.interfacex.module.mission.MissionResolver;
import com.kaylves.interfacex.module.openfeign.OpenFeignResolver;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQListenerResolver;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQProducerResolver;
import com.kaylves.interfacex.module.resolver.*;
import com.kaylves.interfacex.module.rocketmq.impl.*;
import com.kaylves.interfacex.module.xxljob.XXLJobResolver;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.common.InterfaceXProject;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author kaylves
 */
public class InterfaceXHelper {

    public static List<InterfaceXProject> buildRestServiceProjectListUsingResolver(Project project) {
        List<InterfaceXProject> serviceProjectList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {

            Map<String,List<InterfaceXItem>> restServiceItems = buildRestServiceItems(module);

            if(!restServiceItems.isEmpty()){
                serviceProjectList.add(new InterfaceXProject(module,  restServiceItems));
            }
        }

        return serviceProjectList;
    }

    public static Map<String,List<InterfaceXItem>> buildRestServiceItems(Module module) {
        Map<String,List<InterfaceXItem>> serviceItemMap = new LinkedHashMap<>();

        List<ServiceResolver> serviceResolvers = new ArrayList<>();

        //rocketmq
        serviceResolvers.add(new RocketMQTemplateProducerResolver(module));
        serviceResolvers.add(new RocketMQDeliverResolver(module));
        serviceResolvers.add(new RocketMQCustomProducerResolver(module));

        //rocketmq listener
        serviceResolvers.add(new RocketMQListenerResolver(module));
        serviceResolvers.add(new ShardThreadPoolRocketMqListenerResolver(module));


        //rabbitmq
        serviceResolvers.add(new RabbitMQListenerResolver(module));
        serviceResolvers.add(new RabbitMQProducerResolver(module));

        serviceResolvers.add(new XXLJobResolver(module));

        //HTTP
        serviceResolvers.add(new SpringMVCResolver(module));
        serviceResolvers.add(new OpenFeignResolver(module));
        serviceResolvers.add(new MissionResolver(module));

        serviceResolvers.forEach(serviceResolver -> {
            List<InterfaceXItem> interfaceXItemList =  serviceResolver.findServiceItemsInModule();
            if(!interfaceXItemList.isEmpty()){

                interfaceXItemList.sort((item1, item2) -> item1.getUrl().compareTo(item2.getUrl()));

                serviceItemMap.put(serviceResolver.getServiceItemCategory(), interfaceXItemList);
            }
        });
        return serviceItemMap;
    }

    public static List<InterfaceXItem> buildRestServiceItemListUsingResolver(Module module) {
        Map<String,List<InterfaceXItem>> serviceItemMap = buildRestServiceItems(module);
        List<InterfaceXItem> itemList = new ArrayList<>();
        serviceItemMap.values().forEach(itemList::addAll);
        return itemList;
    }


    @NotNull
    public static List<InterfaceXItem> buildRestServiceItemListUsingResolver(Project project) {
        List<InterfaceXItem> itemList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            itemList.addAll(Objects.requireNonNull(buildRestServiceItemListUsingResolver(module)));
        }

        return itemList;
    }

}
