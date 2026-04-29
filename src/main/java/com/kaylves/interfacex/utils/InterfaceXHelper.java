package com.kaylves.interfacex.utils;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.InterfaceProject;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.db.model.ScanResultEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.module.http.HttpItem;
import com.kaylves.interfacex.module.resolver.IServiceResolver;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.navigator.InterfaceXNavigatorState;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Slf4j
public class InterfaceXHelper {

    public static List<InterfaceProject> getInterfaceProjectUsingResolver(Project project, InterfaceXNavigatorState navigatorState) {
        List<InterfaceProject> serviceProjectList = new ArrayList<>();

        Module[] modules = ModuleManager.getInstance(project).getModules();

        for (Module module : modules) {
            Map<String, List<InterfaceItem>> restServiceItems = buildInterfaceItems(module, navigatorState);

            if (!restServiceItems.isEmpty()) {
                serviceProjectList.add(new InterfaceProject(module, restServiceItems));
            }
        }

        return serviceProjectList;
    }

    @NotNull
    public static List<InterfaceItem> buildInterfaceItemUsingResolver(Project project) {
        List<InterfaceItem> itemList = new ArrayList<>();

        InterfaceXNavigatorState xNavigatorState = InterfaceXNavigator.getInstance(project).getXNavigatorState();

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules) {
            itemList.addAll(Objects.requireNonNull(buildInterfaceItemUsingResolver(module, xNavigatorState)));
        }

        return itemList;
    }

    public static List<InterfaceItem> buildInterfaceItemUsingResolver(Module module, InterfaceXNavigatorState xNavigatorState) {
        Map<String, List<InterfaceItem>> serviceItemMap = buildInterfaceItems(module, xNavigatorState);
        List<InterfaceItem> itemList = new ArrayList<>();
        serviceItemMap.values().forEach(itemList::addAll);
        return itemList;
    }

    public static Map<String, List<InterfaceItem>> buildInterfaceItems(Module module, InterfaceXNavigatorState navigatorState) {
        Map<String, List<InterfaceItem>> serviceItemMap = new LinkedHashMap<>();

        List<IServiceResolver> serviceResolvers = ServiceResolverSelector.selectResolvers(module, navigatorState);

        serviceResolvers.forEach(serviceResolver -> {
            long startTime = System.currentTimeMillis();

            List<InterfaceItem> interfaceItemList = serviceResolver.findServiceItemsInModule();

            long endTime = System.currentTimeMillis();

            String category = serviceResolver.getServiceItemCategory();

            if (!interfaceItemList.isEmpty()) {
                if ("Spring-MVC".equals(category)) {
                    PartnerGrouper.groupByPartner(interfaceItemList).forEach((partner, items) -> {
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

    public static List<InterfaceProject> getInterfaceProjectFromCache(Project project, InterfaceXNavigatorState navigatorState) {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        Long lastScanTime = adapter.loadLastScanTime(projectPath);
        if (lastScanTime == null) {
            return null;
        }

        List<ScanResultEntity> cachedResults = adapter.loadScanResults(projectPath);
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
                InterfaceItemCategoryEnum categoryEnum = CategoryParser.parse(entity.getCategory());

                InterfaceItem item = new InterfaceItem(
                        null,
                        categoryEnum,
                        entity.getHttpMethod(),
                        httpItem,
                        true
                );
                item.setModule(module);

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
    }

    public static void saveScanResultsToStorage(Project project, List<InterfaceProject> projects) {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        long scanTime = System.currentTimeMillis();

        List<ScanResultEntity> allEntities = new ArrayList<>();

        for (InterfaceProject interfaceProject : projects) {
            Map<String, List<InterfaceItem>> serviceItemMap = interfaceProject.getServiceItemMap();

            for (Map.Entry<String, List<InterfaceItem>> entry : serviceItemMap.entrySet()) {
                String category = entry.getKey();
                for (InterfaceItem item : entry.getValue()) {
                    ScanResultEntity entity = ScanResultEntity.builder()
                            .projectPath(projectPath)
                            .moduleName(interfaceProject.getModuleName())
                            .category(category)
                            .url(item.getUrl())
                            .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
                            .className(item.getPsiMethod() != null && item.getPsiMethod().getContainingClass() != null
                                    ? item.getPsiMethod().getContainingClass().getQualifiedName() : null)
                            .methodName(item.getPsiMethod() != null ? item.getPsiMethod().getName() : null)
                            .psiElementHash(item.getPsiElement() != null ? item.getPsiElement().hashCode() : 0)
                            .partner(null)
                            .scanTime(scanTime)
                            .build();
                    allEntities.add(entity);
                }
            }
        }

        adapter.saveScanResults(projectPath, allEntities);
        adapter.saveScanMeta(projectPath, scanTime);
        log.info("Saved scan results to storage for project: {}", projectPath);
    }
}
