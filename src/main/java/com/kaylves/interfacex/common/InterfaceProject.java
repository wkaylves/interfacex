package com.kaylves.interfacex.common;

import com.intellij.openapi.module.Module;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author kaylves
 */
@Getter
public class InterfaceProject {

    private final String moduleName;

    public List<InterfaceItem> interfaceItems;

    private final Map<String,List<InterfaceItem>> serviceItemMap;

    public InterfaceProject(Module module, Map<String,List<InterfaceItem>> serviceItemMap) {
        this.moduleName = module.getName();
        this.serviceItemMap = serviceItemMap;
    }

    @Override
    public int hashCode() {
        return this.moduleName.hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if(obj instanceof InterfaceProject){
            return this.moduleName.equals(((InterfaceProject)obj).moduleName);
        }

        return false;
    }

    @Override
    public String toString() {
        return moduleName;
    }
}
