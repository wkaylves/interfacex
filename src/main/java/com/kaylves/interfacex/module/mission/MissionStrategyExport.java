package com.kaylves.interfacex.module.mission;

import com.kaylves.interfacex.common.ExportServiceStrategy;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author kaylves
 */
@Slf4j
public class MissionStrategyExport implements ExportServiceStrategy<ServiceExportBean> {

    @Override
    public List<ServiceExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        return getServiceExportBeanList(project, modules);
    }

    private static @NotNull List<ServiceExportBean> getServiceExportBeanList(Project project, Module[] modules) {
        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();
        Arrays.stream(modules).forEach(module -> {
            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(MissionClientAnnotation.MISSION_CLIENT_ANNOTATION.getShortName(), project, globalSearchScope);

            for (PsiAnnotation psiAnnotation : psiAnnotations) {
                buildElements(module, psiAnnotation, globalSearchScope, serviceExportBeans);
            }
        });

        return serviceExportBeans;
    }

    private static void buildElements(Module module, PsiAnnotation psiAnnotation, GlobalSearchScope globalSearchScope, List<ServiceExportBean> serviceExportBeans) {
        PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();

        PsiElement psiElement = psiModifierList.getParent();
        PsiClass psiClass = (PsiClass) psiElement;

        //过滤测试包类文件
        if (PsiAnnotationHelper.isTestPackage(psiClass)) {
            return;
        }

        String serviceName = PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value");

        PsiMethod[] psiMethods = psiClass.getMethods();

        for (PsiMethod psiMethod : psiMethods) {

            String name = IdeaPluginUtils.obtainDocAsString(psiMethod);

            Boolean result = ReferencesSearch.search(psiMethod, globalSearchScope).findFirst() != null;

            PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                for (MissionClientMethodAnnotation mappingAnnotation : MissionClientMethodAnnotation.values()) {

                    if (mappingAnnotation.getQualifiedName().equals(annotation.getQualifiedName())) {
                        PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path").forEach(path ->
                                serviceExportBeans.add(ServiceExportBean.builder()
                                        .modelName(module.getName())
                                        .dependencyService(serviceName)
                                        .interfaceType("MissionClient")
                                        .path(path).pathName(name)
                                        .auth(IdeaPluginUtils
                                        .obtainAuth(psiClass.getDocComment()))
                                        .usedAble(result)
                                        .fullClassName(psiClass.getQualifiedName())
                                        .simpleClassName(psiClass.getName())
                                        .build()));
                    }

                }
            }
        }
    }
}
