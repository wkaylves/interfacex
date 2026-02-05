package com.kaylves.interfacex.ui.action.search;

import com.kaylves.interfacex.ui.navigator.ServiceItem;
import com.kaylves.interfacex.service.ServiceHelper;
import com.intellij.navigation.ChooseByNameContributor;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GotoRequestMappingContributor implements ChooseByNameContributor {

    Module myModule;

    private List<ServiceItem> navItem;

    public GotoRequestMappingContributor(Module myModule) {
        this.myModule = myModule;
    }

    @NotNull
    @Override
    public String[] getNames(Project project, boolean onlyThisModuleChecked) {
        String[] names = null;
        List<ServiceItem> itemList;
        ///todo find all rest url file in project
        if (onlyThisModuleChecked && myModule != null) {
            itemList = ServiceHelper.buildRestServiceItemListUsingResolver(myModule);
        } else {
            itemList = ServiceHelper.buildRestServiceItemListUsingResolver(project);
        }

        navItem = itemList;

        names = new String[itemList.size()];

        for (int i = 0; i < itemList.size(); i++) {
            ServiceItem requestMappingNavigationItem = itemList.get(i);
            names[i] = requestMappingNavigationItem.getName();
        }

        return names;
    }

    @NotNull
    @Override
    public NavigationItem[] getItemsByName(String name, String pattern, Project project, boolean onlyThisModuleChecked) {
        return navItem.stream().filter(item -> {
            assert item.getName() != null;
            return item.getName().equals(name);
        }).toArray(NavigationItem[]::new);
    }
}
