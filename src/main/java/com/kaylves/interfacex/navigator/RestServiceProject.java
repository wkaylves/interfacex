package com.kaylves.interfacex.navigator;

import com.intellij.openapi.module.Module;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RestServiceProject {

    private String port = "8080";

    private String appName;

    private String moduleName;

    private Module module;

    private String applicationClass;

    public List<RestServiceItem> serviceItems;

    private Map<String,List<RestServiceItem>> serviceItemMap;

    public RestServiceProject(Module module, Map<String,List<RestServiceItem>> serviceItemMap) {
        this.moduleName = module.getName();
        appName = moduleName;
        this.serviceItemMap = serviceItemMap;
    }

    @Override
    public String toString() {
        return appName + ":" + port;
    }
}
