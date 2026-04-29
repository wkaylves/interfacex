package com.kaylves.interfacex.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.InterfaceProject;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.common.constants.InterfaceXItemEnum;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.ScanMetaDao;
import com.kaylves.interfacex.db.dao.ScanResultDao;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import com.kaylves.interfacex.module.http.HttpItem;
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

import java.sql.SQLException;
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
        for (InterfaceItemConfigEntity entity : configEntities) {
            // 假设实体的 isEnabled() 方法表示该项是否被勾选
            if (entity.getEnabled()) {
                enabledCategories.add(entity.getItemCategory());
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

        // RocketMQ Producer (Custom)
        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQProducer.name())) {
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

        // RabbitMQTemplate
        if (enabledCategories.contains(InterfaceXItemEnum.RabbitMQTemplate.name())) {
            serviceResolvers.add(new RabbitMQProducerResolverServiceResolver(module));
        }

        // RocketMQTemplate
        if (enabledCategories.contains(InterfaceXItemEnum.RocketMQTemplate.name())) {
            serviceResolvers.add(new RocketMQTemplateProducerServiceResolver(module));
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
        serviceResolvers.add(new RocketMQTemplateProducerServiceResolver(module));
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

            String category = serviceResolver.getServiceItemCategory();

            if (!interfaceItemList.isEmpty()) {
                // 按@partner 注释分组（仅对 Spring-MVC 生效）
                if ("Spring-MVC".equals(category)) {
                    groupByPartner(interfaceItemList).forEach((partner, items) -> {
                        items.sort((item1, item2) -> item1.getUrl().compareTo(item2.getUrl()));
                        serviceItemMap.put(partner, items);
                    });
                } else {
                    interfaceItemList.sort((item1, item2) -> item1.getUrl().compareTo(item2.getUrl()));
                    serviceItemMap.put(category, interfaceItemList);
                }
            }

            log.info("module {} serviceItem :{} used {} ms", module.getName(), category, (endTime - startTime));
        });
        return serviceItemMap;
    }

    /**
     * 按类注释中的@partner 标签对 InterfaceItem 进行分组
     *
     * @param itemList InterfaceItem 列表
     * @return 按 partner 分组的 Map
     */
    private static Map<String, List<InterfaceItem>> groupByPartner(List<InterfaceItem> itemList) {
        Map<String, List<InterfaceItem>> groupedMap = new LinkedHashMap<>();

        for (InterfaceItem item : itemList) {
            String partner = getPartnerFromItem(item);
            if (partner == null || partner.isEmpty()) {
                partner = "Spring-MVC";
            }else{
                partner+="-HTTP";
            }

            groupedMap.computeIfAbsent(partner, k -> new ArrayList<>()).add(item);
        }

        return groupedMap;
    }

    /**
     * 从 InterfaceItem 获取类注释中的@partner 标签内容
     *
     * @param item InterfaceItem
     * @return partner 内容
     */
    private static String getPartnerFromItem(InterfaceItem item) {
        if (item.getPsiMethod() == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocComment docComment = item.getPsiMethod().getDocComment();
        if (docComment == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocTag partnerTag = docComment.findTagByName("partner");
        if (partnerTag == null) {
            return null;
        }

        com.intellij.psi.javadoc.PsiDocTagValue psiDocTagValue = partnerTag.getValueElement();
        return psiDocTagValue != null ? psiDocTagValue.getText() : null;
    }

    /**
     * 将数据库中的 category 字符串转换为枚举
     * 处理历史数据兼容性问题(如 "Spring-MVC" -> HTTP)
     */
    private static InterfaceItemCategoryEnum parseCategoryEnum(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }
        
        // 处理历史数据映射
        switch (category) {
            case "Spring-MVC":
            case "Jakarta":
            case "Jaxrs":
            case "RestTemplate":
                return InterfaceItemCategoryEnum.HTTP;
            
            case "RabbitMQ-Listener":
                return InterfaceItemCategoryEnum.RabbitMQListener;
            
            case "RabbitMQ-Producer":
                return InterfaceItemCategoryEnum.RabbitMQProducer;
            
            case "RocketMQ-Producer":
            case "RocketMQTemplate-Producer":
                return InterfaceItemCategoryEnum.RocketMQProducer;
            
            case "RocketMQ-Deliver":
                return InterfaceItemCategoryEnum.RocketMQDeliver;
            
            case "RocketMqListener":
            case "ShardRocketMqListener":
                return InterfaceItemCategoryEnum.RocketMQListener;
            
            case "MissionClient":
                return InterfaceItemCategoryEnum.Mission;
            
            default:
                try {
                    return InterfaceItemCategoryEnum.valueOf(category);
                } catch (IllegalArgumentException e) {
                    log.warn("Unknown category: {}, defaulting to HTTP", category);
                    return InterfaceItemCategoryEnum.HTTP;
                }
        }
    }

    public static List<InterfaceProject> getInterfaceProjectFromCache(Project project, InterfaceXNavigatorState navigatorState) {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        ScanResultDao scanResultDao = dbService.getScanResultDao();
        ScanMetaDao scanMetaDao = dbService.getScanMetaDao();

        String projectPath = project.getBasePath();

        try {
            Long lastScanTime = scanMetaDao.getLastScanTime(projectPath);
            if (lastScanTime == null) {
                return null;
            }

            List<ScanResultEntity> cachedResults = scanResultDao.findByProjectPath(projectPath);
            if (cachedResults.isEmpty()) {
                return null;
            }

            Map<String, List<ScanResultEntity>> groupedByModule = new LinkedHashMap<>();
            for (ScanResultEntity entity : cachedResults) {
                groupedByModule.computeIfAbsent(entity.getModuleName(), k -> new ArrayList<>()).add(entity);
            }

            List<InterfaceProject> projects = new ArrayList<>();
            Module[] modules = ModuleManager.getInstance(project).getModules();

            for (Module module : modules) {
                List<ScanResultEntity> moduleResults = groupedByModule.get(module.getName());
                if (moduleResults == null || moduleResults.isEmpty()) {
                    continue;
                }

                Map<String, List<InterfaceItem>> moduleServiceMap = new LinkedHashMap<>();
                for (ScanResultEntity entity : moduleResults) {
                    HttpItem httpItem = HttpItem.builder().url(entity.getUrl()).build();
                    InterfaceItemCategoryEnum categoryEnum = parseCategoryEnum(entity.getCategory());
                    
                    // 从缓存加载时,PsiElement 为 null,需要在双击时重新查找
                    InterfaceItem item = new InterfaceItem(
                            null,  // psiMethod 为 null
                            categoryEnum,
                            entity.getHttpMethod(),
                            httpItem,
                            true
                    );
                    item.setModule(module);
                    
                    // 存储 className 和 methodName,用于智能查找
                    if (entity.getClassName() != null && entity.getMethodName() != null) {
                        item.setCachedClassName(entity.getClassName());
                        item.setCachedMethodName(entity.getMethodName());
                        log.info("Loaded from cache: {}.{}", entity.getClassName(), entity.getMethodName());
                    } else {
                        log.warn("Cache data missing className/methodName for URL: {}, className={}, methodName={}", 
                                entity.getUrl(), entity.getClassName(), entity.getMethodName());
                    }
                    
                    moduleServiceMap.computeIfAbsent(entity.getCategory(), k -> new ArrayList<>()).add(item);
                }

                projects.add(new InterfaceProject(module, moduleServiceMap));
            }

            return projects;
        } catch (SQLException e) {
            log.error("Failed to load from SQLite cache", e);
            return null;
        }
    }

    public static void saveScanResultsToDatabase(Project project, List<InterfaceProject> projects) {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        dbService.initialize();
        ScanResultDao scanResultDao = dbService.getScanResultDao();
        ScanMetaDao scanMetaDao = dbService.getScanMetaDao();

        String projectPath = project.getBasePath();
        long scanTime = System.currentTimeMillis();

        try {
            // 记录扫描结果总数
            int totalProjects = projects != null ? projects.size() : 0;
            int totalEntities = 0;
            
            log.info("Starting to save scan results for project: {}, total projects: {}", projectPath, totalProjects);
            
            scanResultDao.deleteByProjectPath(projectPath);

            if (projects != null) {
                for (InterfaceProject interfaceProject : projects) {
                    List<ScanResultEntity> entities = new ArrayList<>();
                    Map<String, List<InterfaceItem>> serviceItemMap = interfaceProject.getServiceItemMap();

                    log.debug("Processing module: {}, service categories: {}", 
                            interfaceProject.getModuleName(), 
                            serviceItemMap != null ? serviceItemMap.keySet() : "null");

                    if (serviceItemMap != null) {
                        for (Map.Entry<String, List<InterfaceItem>> entry : serviceItemMap.entrySet()) {
                            String category = entry.getKey();
                            List<InterfaceItem> items = entry.getValue();
                            
                            log.debug("Category: {}, items count: {}", category, items != null ? items.size() : 0);
                            
                            if (items != null) {
                                for (InterfaceItem item : items) {
                                    String className = null;
                                    String methodName = null;
                                    
                                    if (item.getPsiMethod() != null && item.getPsiMethod().getContainingClass() != null) {
                                        className = item.getPsiMethod().getContainingClass().getQualifiedName();
                                        methodName = item.getPsiMethod().getName();
                                    } else if (item.getCachedClassName() != null && item.getCachedMethodName() != null) {
                                        className = item.getCachedClassName();
                                        methodName = item.getCachedMethodName();
                                    }

                                    log.debug("ClassName: {}, methodName: {}", className, methodName);

                                    ScanResultEntity entity = ScanResultEntity.builder()
                                            .projectPath(projectPath)
                                            .moduleName(interfaceProject.getModuleName())
                                            .category(category)
                                            .url(item.getUrl())
                                            .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
                                            .className(className)
                                            .methodName(methodName)
                                            .psiElementHash(item.getPsiElement() != null ? item.getPsiElement().hashCode() : 0)
                                            .partner(null)
                                            .scanTime(scanTime)
                                            .build();
                                    entities.add(entity);
                                }
                            }
                        }
                    }

                    if (!entities.isEmpty()) {
                        log.info("Batch inserting {} entities for module: {}", entities.size(), interfaceProject.getModuleName());
                        scanResultDao.batchUpsert(projectPath, entities);
                        totalEntities += entities.size();
                    } else {
                        log.warn("No entities to insert for module: {}", interfaceProject.getModuleName());
                    }
                }
            }

            scanMetaDao.updateScanTime(projectPath, scanTime);
            log.info("Saved {} scan results to SQLite for project: {}", totalEntities, projectPath);
        } catch (SQLException e) {
            log.error("Failed to save scan results to SQLite for project: " + projectPath, e);
            throw new RuntimeException("Failed to save scan results", e);
        }
    }

}
