package com.kaylves.interfacex.ui.action.search;

import com.intellij.ide.util.gotoByName.ChooseByNameFilterConfiguration;
import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.annotations.InterfaceXEnum;

/**
 * Configuration for file type filtering popup in "Go to | Service" action.
 *
 * @author zhaow
 */
@State(name = "GotoRequestMappingConfiguration", storages = @Storage(StoragePathMacros.WORKSPACE_FILE))
@Service
final class GotoRequestMappingConfiguration extends ChooseByNameFilterConfiguration<InterfaceXEnum> {

    /**
     * Get configuration instance
     *
     * @param project a project instance
     * @return a configuration instance
     */
    public static GotoRequestMappingConfiguration getInstance(Project project) {
        return ServiceManager.getService(project, GotoRequestMappingConfiguration.class);
    }

    @Override
    protected String nameForElement(InterfaceXEnum type) {
        return type.name();
    }
}
