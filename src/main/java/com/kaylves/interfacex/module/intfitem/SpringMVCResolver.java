package com.kaylves.interfacex.module.intfitem;

import com.kaylves.interfacex.common.annotations.http.PathMappingAnnotation;
import com.kaylves.interfacex.common.annotations.http.SpringControllerAnnotation;
import com.kaylves.interfacex.utils.spring.SpringRequestMappingAnnotationHelper;
import com.kaylves.interfacex.common.method.RequestPath;
import com.kaylves.interfacex.module.navigator.RestServiceItem;
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
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();

        SpringControllerAnnotation[] supportedAnnotations = SpringControllerAnnotation.values();
        for (PathMappingAnnotation controllerAnnotation : supportedAnnotations) {
            filterSpringList(project, globalSearchScope, controllerAnnotation, itemList);
        }
        return itemList;
    }

    private void filterSpringList(Project project, GlobalSearchScope globalSearchScope, PathMappingAnnotation controllerAnnotation, List<RestServiceItem> itemList) {
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

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {
        PsiMethod[] psiMethods = psiClass.getMethods();

        List<RestServiceItem> itemList = new ArrayList<>();
        List<RequestPath> classRequestPaths = SpringRequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

        for (PsiMethod psiMethod : psiMethods) {

            RequestPath[] methodRequestPaths = SpringRequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);

            assert classRequestPaths != null;

            for (RequestPath classRequestPath : classRequestPaths) {

                for (RequestPath methodRequestPath : methodRequestPaths) {
                    String path = classRequestPath.getPath();
                    RestServiceItem item = createRestServiceItem(psiMethod, path, methodRequestPath);
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
