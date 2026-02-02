package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.http.JakartaPathAnnotation;
import com.kaylves.interfacex.common.jakarta.JakartaAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.ui.navigator.ServiceItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JakartaResolver extends BaseServiceResolver {

    public JakartaResolver(Module module) {
        this.module = module;
    }

    public JakartaResolver(Project project) {
        this.project = project;
    }

    @Override
    public List<ServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<ServiceItem> itemList = new ArrayList<>();
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
                    ServiceItem item = createRestServiceItem(psiMethod, InterfaceXEnum.HTTP, classUriPath, methodUriPath, false);
                    itemList.add(item);
                }
            }
        }
        return itemList;
    }

    @Override
    public String getServiceItem() {
        return "Jakarta";
    }
}
