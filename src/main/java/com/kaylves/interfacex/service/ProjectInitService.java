package com.kaylves.interfacex.service;

import com.kaylves.interfacex.common.InterfaceXProject;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.utils.InterfaceXHelper;

import java.util.List;

/**
 * @author baihua.huang
 */
@Service(Service.Level.PROJECT)
public final class ProjectInitService implements Disposable {

    private final Project project;

    public ProjectInitService(Project project) {
        this.project = project;
    }

    public static ProjectInitService getInstance(Project p) {
        return p.getService(ProjectInitService.class);
    }

    public List<InterfaceXProject> getServiceProjects() {
        return DumbService
                .getInstance(project)
                .runReadActionInSmartMode(() ->
                        InterfaceXHelper.buildRestServiceProjectListUsingResolver(project)
                );
    }

    @Override
    public void dispose() {
    }
}
