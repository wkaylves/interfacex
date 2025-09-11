package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.navigator.RestServiceItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseServiceResolver implements ServiceResolver {

    public static final Logger LOG = Logger.getInstance(BaseServiceResolver.class);

    Module module;

    Project project;

    @Override
    public List<RestServiceItem> findServiceItemsInModule() {
        List<RestServiceItem> itemList = new ArrayList<>();

        if (module == null) {
            return itemList;
        }

        GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

        return getRestServiceItemList(module.getProject(), globalSearchScope);
    }

    public abstract List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope);

    @NotNull
    protected RestServiceItem createRestServiceItem(PsiElement psiMethod, String classUriPath, RequestPath requestMapping) {
        return createRestServiceItem(psiMethod, classUriPath, requestMapping, true);
    }

    @NotNull
    protected RestServiceItem createRestServiceItem(PsiElement psiMethod, String classUriPath, RequestPath requestMapping, Boolean isUrlWithoutReqMethod) {

        LOG.debug("psiMethod:{},classUriPath:{},requestMapping:{},isUrlWithoutReqMethod:{}", psiMethod, classUriPath, requestMapping, isUrlWithoutReqMethod);

        if (!classUriPath.startsWith("/")) {
            classUriPath = "/".concat(classUriPath);
        }

        if (!classUriPath.endsWith("/")) {
            classUriPath = classUriPath.concat("/");
        }

        String methodPath = requestMapping.getPath();

        if (methodPath.startsWith("/")) {
            methodPath = methodPath.substring(1);
        }

        String requestPath = classUriPath + methodPath;

        RestServiceItem item = new RestServiceItem(psiMethod, requestMapping.getMethod(), requestPath, isUrlWithoutReqMethod);

        if (module != null) {
            item.setModule(module);
        }

        LOG.info("item:" + item);
        return item;
    }
}
