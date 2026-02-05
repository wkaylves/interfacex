package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.http.PathMappingAnnotation;
import com.kaylves.interfacex.annotations.http.SpringControllerAnnotation;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.ui.navigator.ServiceItem;
import com.kaylves.interfacex.utils.PropertiesHandler;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class SpringMVCResolver extends BaseServiceResolver {

    PropertiesHandler propertiesHandler;

    public SpringMVCResolver(Module module) {
        this.module = module;
        propertiesHandler = new PropertiesHandler(module);
    }

    @Override
    public List<ServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<ServiceItem> itemList = new ArrayList<>();

        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        for (PathMappingAnnotation controllerAnnotation : supportedAnnotations) {
            filterSpringList(project, globalSearchScope, controllerAnnotation, itemList);
        }
        return itemList;
    }

    private void filterSpringList(Project project, GlobalSearchScope globalSearchScope, PathMappingAnnotation controllerAnnotation, List<ServiceItem> itemList) {
        // java:
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(controllerAnnotation.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if(PsiAnnotationHelper.isTestPackage(psiClass)){
                continue;
            }

            itemList.addAll(getServiceItemList(psiClass));
        }
    }

    protected List<ServiceItem> getServiceItemList(PsiClass psiClass) {
        PsiMethod[] psiMethods = psiClass.getMethods();

        List<ServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {

            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);

            assert classRequestPaths != null;

            for (RequestPath classRequestPath : classRequestPaths) {

                for (RequestPath methodRequestPath : methodRequestPaths) {
                    String path = classRequestPath.getPath();
                    ServiceItem item = createRestServiceItem(psiMethod, InterfaceXEnum.HTTP, path, methodRequestPath);
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }



    @Override
    public String getServiceItem() {
        return "Spring-MVC";
    }
}
