package com.kaylves.interfacex.module.intfitem;

import com.intellij.psi.search.searches.ReferencesSearch;
import com.kaylves.interfacex.common.annotations.InterfaceXEnum;
import com.kaylves.interfacex.common.annotations.http.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.spring.SpringRequestMappingAnnotationHelper;
import com.kaylves.interfacex.common.method.RequestPath;
import com.kaylves.interfacex.module.navigator.RestServiceItem;
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

            itemList.addAll(getServiceItemList(psiClass,globalSearchScope));
        }

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass,GlobalSearchScope globalSearchScope) {

        List<RequestPath> classRequestPaths = SpringRequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

        PsiMethod[] psiMethods = psiClass.getMethods();
        List<RestServiceItem> itemList = new ArrayList<>();

        for (PsiMethod psiMethod : psiMethods) {
            RequestPath[] methodRequestPaths = SpringRequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiMethod);
            assert classRequestPaths != null;

            boolean result = ReferencesSearch.search(psiMethod, globalSearchScope).findFirst() != null;

            if(!result){
                continue;
            }

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
        return InterfaceXEnum.OpenFeign.name();
    }
}
