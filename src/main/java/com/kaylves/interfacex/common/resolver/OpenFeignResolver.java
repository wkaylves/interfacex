package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.http.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.navigator.RestServiceItem;
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
public class OpenFeignResolver extends BaseServiceResolver {

    public OpenFeignResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(SpringHttpRequestAnnotation.FEIGN_CLIENT.getShortName(), project, globalSearchScope);

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

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {

        List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

        PsiMethod[] psiMethods = psiClass.getMethods();
        List<RestServiceItem> itemList = new ArrayList<>();

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);
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
        return "OpenFeign";
    }
}
