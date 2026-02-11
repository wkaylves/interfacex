package com.kaylves.interfacex.module.xxljob;

import com.kaylves.interfacex.common.ExportServiceStrategy;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class XXLJobExportServiceStrategy implements ExportServiceStrategy<ServiceExportBean> {

    @Override
    public List<ServiceExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();


        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {
            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            List<ServiceExportBean> callList = ReadAction.compute(() -> filters(project, module, globalSearchScope));

            serviceExportBeans.addAll(callList);

        });

        return serviceExportBeans;
    }

    public List<ServiceExportBean> filters(Project project, Module module, GlobalSearchScope globalSearchScope) {

        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        Collection<PsiAnnotation> psiAnnotations = new ArrayList<>();

        Arrays.stream(XXLJobComponentAnnotation.values()).forEach(xxlJobComponentAnnotation -> {
            Collection<PsiAnnotation> psiAnnotationCollection = JavaAnnotationIndex.getInstance().get(xxlJobComponentAnnotation.getShortName(), project, globalSearchScope);
            if (!psiAnnotationCollection.isEmpty()) {
                psiAnnotations.addAll(psiAnnotationCollection);
            }
        });

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                continue;
            }

            String path = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value"));

            serviceExportBeans.add(ServiceExportBean.builder()
                    .modelName(module.getName())
                    .interfaceType("XXL-JOB")
                    .path(path)
                    .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                    .pathName(IdeaPluginUtils.obtainDocAsString(psiClass.getDocComment()))
                    .fullClassName(psiClass.getQualifiedName())
                    .simpleClassName(psiClass.getName())
                    .build());
        }

        return serviceExportBeans;
    }
}
