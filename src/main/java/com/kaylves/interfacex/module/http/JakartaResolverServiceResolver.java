package com.kaylves.interfacex.module.http;

import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.module.http.jakarta.JakartaAnnotationHelper;
import com.kaylves.interfacex.module.http.method.RequestPath;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.InterfaceItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JakartaResolverServiceResolver extends BaseServiceResolver {

    public JakartaResolverServiceResolver(Module module) {
        this.module = module;
    }

    public JakartaResolverServiceResolver(Project project) {
        this.project = project;
    }

    @Override
    public List<InterfaceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(JakartaPathAnnotation.PATH.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass)) {
                continue;
            }

            PsiClass psiClass = (PsiClass) psiElement;

            PsiMethod[] psiMethods = psiClass.getMethods();

            String classUriPath = JakartaAnnotationHelper.getClassUriPath(psiClass);

            for (PsiMethod psiMethod : psiMethods) {
                RequestPath[] methodUriPaths = JakartaAnnotationHelper.getRequestPaths(psiMethod);

                for (RequestPath methodUriPath : methodUriPaths) {
                    InterfaceItem item = createRestServiceItem(psiMethod, InterfaceItemCategoryEnum.HTTP, classUriPath, methodUriPath, false);
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    @Override
    public String getServiceItemCategory() {
        return "Jakarta";
    }
}
