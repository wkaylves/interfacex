package com.kaylves.interfacex.module.rabbitmq.impl;

import com.kaylves.interfacex.common.ExportServiceStrategy;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.module.rabbitmq.RabbitMQAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author kaylves
 */
@Slf4j
public class RabbitMQStrategyExport implements ExportServiceStrategy<ServiceExportBean> {

    @Override
    public List<ServiceExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {

            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            List<ServiceExportBean> callList = ReadAction.compute(() -> filter(project, module, globalSearchScope));
            serviceExportBeans.addAll(callList);
        });
        return serviceExportBeans;
    }

    public List<ServiceExportBean> filter(Project project, Module module, GlobalSearchScope globalSearchScope) {

        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RabbitMQAnnotation.PATH.getShortName(), project, globalSearchScope);

        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {

            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (psiElement instanceof PsiMethod psiMethod) {

                PsiClass psiClass = (PsiClass) psiMethod.getParent();

                //过滤测试包类文件
                if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                    continue;
                }

                String queue = PsiAnnotationHelper.getOneQueue(psiAnnotation);

                String path = MessageFormat.format("{0}", queue);

                serviceExportBeans.add(ServiceExportBean.builder()
                        .modelName(module.getName())
                        .interfaceType("RabbitMQ_Listener")
                        .path(path)
                        .pathName(IdeaPluginUtils.obtainDocAsString(psiClass.getDocComment()))
                        .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                        .fullClassName(psiClass.getQualifiedName())
                        .simpleClassName(psiClass.getName()).build());
            }

        }

        return serviceExportBeans;
    }

}
