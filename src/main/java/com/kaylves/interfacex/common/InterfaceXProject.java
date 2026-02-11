package com.kaylves.interfacex.common;

import com.intellij.openapi.module.Module;
import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class InterfaceXProject {

    private final String moduleName;

    public List<InterfaceXItem> interfaceXItems;

    private final Map<String,List<InterfaceXItem>> serviceItemMap;

    public InterfaceXProject(Module module, Map<String,List<InterfaceXItem>> serviceItemMap) {
        this.moduleName = module.getName();
        this.serviceItemMap = serviceItemMap;
    }

    @Override
    public int hashCode() {
        return this.moduleName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof InterfaceXProject){
            return this.moduleName.equals(((InterfaceXProject)obj).moduleName);
        }

        return false;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}
