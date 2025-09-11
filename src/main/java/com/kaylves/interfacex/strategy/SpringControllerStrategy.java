package com.kaylves.interfacex.strategy;

import com.kaylves.interfacex.annotations.http.SpringControllerAnnotation;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.IntfxUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.*;

/**
 * @author kaylves
 */
public class SpringControllerStrategy implements ServiceStrategy<ServiceExportBean> {

    @Override
    public List<ServiceExportBean> obtainServiceExportBeans(Project project) {

        return ReadAction.compute(() -> {

            Module[] modules = ModuleManager.getInstance(project).getModules();

            List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

            Arrays.stream(modules).forEach(module -> {
                GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

                String interfaceType = "Spring-MVC";

                Arrays.stream(SpringControllerAnnotation.values()).forEach(springControllerAnnotation -> {
                    Collection<PsiAnnotation> psiAnnotations;

                    psiAnnotations = JavaAnnotationIndex.getInstance().get(springControllerAnnotation.getShortName(), project, globalSearchScope);

                    for (PsiAnnotation psiAnnotation : psiAnnotations) {

                        PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();

                        PsiElement psiElement = psiModifierList.getParent();
                        PsiClass psiClass = (PsiClass) psiElement;

                        //过滤测试包类文件
                        if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                            continue;
                        }

                        String serviceName = PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value");

                        PsiMethod[] psiMethods = psiClass.getMethods();

                        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

                        for (PsiMethod psiMethod : psiMethods) {

                            String name = IdeaPluginUtils.obtainDocAsString(psiMethod);

                            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);
                            assert classRequestPaths != null;

                            for (RequestPath classRequestPath : classRequestPaths) {
                                for (RequestPath methodRequestPath : methodRequestPaths) {
                                    String requestPath = IntfxUtils.getRequestPath(classRequestPath, methodRequestPath);
                                    serviceExportBeans.add(ServiceExportBean.builder()
                                            .modelName(module.getName())
                                            .dependencyService(serviceName)
                                            .interfaceType(interfaceType)
                                            .path(requestPath)
                                            .pathName(name)
                                            .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                                            .fullClassName(psiClass.getQualifiedName())
                                            .simpleClassName(psiClass.getName())
                                            .build());
                                }
                            }
                        }
                    }
                });


            });

            return serviceExportBeans;
        });
    }
}
