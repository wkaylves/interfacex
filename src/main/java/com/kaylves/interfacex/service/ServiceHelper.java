package com.kaylves.interfacex.service;

import com.kaylves.interfacex.common.resolver.*;
import com.kaylves.interfacex.ui.navigator.ServiceItem;
import com.kaylves.interfacex.ui.navigator.RestServiceProject;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author kaylves
 */
public class ServiceHelper {

    public static List<RestServiceProject> buildRestServiceProjectListUsingResolver(Project project) {
        List<RestServiceProject> serviceProjectList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {

            Map<String,List<ServiceItem>> restServiceItems = buildRestServiceItems(module);

            if(!restServiceItems.isEmpty()){
                serviceProjectList.add(new RestServiceProject(module,  restServiceItems));
            }
        }

        return serviceProjectList;
    }

    public static Map<String,List<ServiceItem>> buildRestServiceItems(Module module) {
        Map<String,List<ServiceItem>> serviceItemMap = new LinkedHashMap<>();

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
            List<ServiceItem> serviceItemList =  serviceResolver.findServiceItemsInModule();
            if(!serviceItemList.isEmpty()){

                serviceItemList.sort((item1, item2) -> item1.getUrl().compareTo(item2.getUrl()));

                serviceItemMap.put(serviceResolver.getServiceItem(), serviceItemList);
            }
        });
        return serviceItemMap;
    }

    public static List<ServiceItem> buildRestServiceItemListUsingResolver(Module module) {
        Map<String,List<ServiceItem>> serviceItemMap = buildRestServiceItems(module);
        List<ServiceItem> itemList = new ArrayList<>();
        serviceItemMap.values().forEach(itemList::addAll);
        return itemList;
    }


    @NotNull
    public static List<ServiceItem> buildRestServiceItemListUsingResolver(Project project) {
        List<ServiceItem> itemList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            itemList.addAll(Objects.requireNonNull(buildRestServiceItemListUsingResolver(module)));
        }

        return itemList;
    }

}
