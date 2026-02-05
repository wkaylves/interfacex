package com.kaylves.interfacex.module.resolver;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.module.http.method.RequestPath;
import com.kaylves.interfacex.common.InterfaceXItem;
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

    protected Module module;

    protected Project project;

    @Override
    public List<InterfaceXItem> findServiceItemsInModule() {
        List<InterfaceXItem> itemList = new ArrayList<>();

        if (module == null) {
            return itemList;
        }

        GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

        return getRestServiceItemList(module.getProject(), globalSearchScope);
    }

    public abstract List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope);

    @NotNull
    protected InterfaceXItem createRestServiceItem(PsiElement psiMethod, InterfaceXItemCategoryEnum interfaceXItemCategoryEnum, String classUriPath, RequestPath requestMapping) {
        return createRestServiceItem(psiMethod, interfaceXItemCategoryEnum, classUriPath, requestMapping, true);
    }

    @NotNull
    protected InterfaceXItem createRestServiceItem(PsiElement psiMethod, InterfaceXItemCategoryEnum interfaceXItemCategoryEnum, String classUriPath, RequestPath requestMapping, Boolean isUrlWithoutReqMethod) {

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

        InterfaceXItem item = new InterfaceXItem(psiMethod, interfaceXItemCategoryEnum, requestMapping.getMethod(), requestPath, isUrlWithoutReqMethod);

        if (module != null) {
            item.setModule(module);
        }

        LOG.info("item:" + item);
        return item;
    }
}
