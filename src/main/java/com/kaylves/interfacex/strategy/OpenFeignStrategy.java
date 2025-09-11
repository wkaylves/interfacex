package com.kaylves.interfacex.strategy;

import com.kaylves.interfacex.annotations.http.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.IntfxUtils;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class OpenFeignStrategy implements ServiceStrategy<ServiceExportBean> {
    @Override
    public List<ServiceExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {

            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance()
                    .get(SpringHttpRequestAnnotation.FEIGN_CLIENT.getShortName(), project, globalSearchScope);

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

                    Boolean result = ReferencesSearch.search(psiMethod, globalSearchScope).findFirst() != null;

                    RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);
                    assert classRequestPaths != null;

                    for (RequestPath classRequestPath : classRequestPaths) {
                        for (RequestPath methodRequestPath : methodRequestPaths) {
                            String requestPath = IntfxUtils.getRequestPath(classRequestPath, methodRequestPath);
                            serviceExportBeans.add(ServiceExportBean.builder()
                                    .modelName(module.getName())
                                    .dependencyService(serviceName)
                                    .interfaceType("openFeign")
                                    .path(requestPath)
                                    .pathName(name)
                                    .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                                    .usedAble(result)
                                    .fullClassName(psiClass.getQualifiedName())
                                    .simpleClassName(psiClass.getName())
                                    .build());
                        }
                    }
                }
            }

        });

        return serviceExportBeans;
    }
}
