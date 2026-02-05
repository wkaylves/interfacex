package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.module.Module;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class RestServiceProject{

    private final String moduleName;

    private Module module;

    public List<ServiceItem> serviceItems;

    private final Map<String,List<ServiceItem>> serviceItemMap;

    public RestServiceProject(Module module, Map<String,List<ServiceItem>> serviceItemMap) {
        this.moduleName = module.getName();
        this.serviceItemMap = serviceItemMap;
    }

    @Override
    public int hashCode() {
        return this.moduleName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof RestServiceProject){
            return this.moduleName.equals(((RestServiceProject)obj).moduleName);
        }

        return false;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}
