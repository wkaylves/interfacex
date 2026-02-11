package com.kaylves.interfacex.common;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.utils.ModuleHelper;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.intellij.navigation.ItemPresentation;
import com.intellij.navigation.NavigationItem;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Computable;
import com.intellij.pom.Navigatable;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

@ToString
public class InterfaceXItem implements NavigationItem,InterfaceXUrl {

    private PsiMethod psiMethod;

    private final PsiElement psiElement;

    private Module module;

    private final String requestMethod;

    private HttpMethod method;

    @Getter
    private InterfaceXItemCategoryEnum interfaceXItemCategoryEnum;

    @Getter
    private final InterfaceXUrl originalItem;

    private Navigatable navigationElement;

    private Boolean isUrlWithoutReqMethod = false;

    public InterfaceXItem(PsiElement psiElement, InterfaceXItemCategoryEnum interfaceXItemCategoryEnum, String requestMethod, InterfaceXUrl url, Boolean isUrlWithoutReqMethod) {
        this.psiElement = psiElement;
        this.interfaceXItemCategoryEnum = interfaceXItemCategoryEnum;

        if (psiElement instanceof PsiMethod) {
            this.psiMethod = (PsiMethod) psiElement;
        }

        this.requestMethod = requestMethod;

        if (requestMethod != null) {
            method = HttpMethod.getByRequestMethod(requestMethod);
        }

        this.originalItem = url;

        if (psiElement instanceof Navigatable) {
            navigationElement = (Navigatable) psiElement;
        }

        this.isUrlWithoutReqMethod = isUrlWithoutReqMethod;
    }

    @Override
    public String getUrl() {
        return this.originalItem.getUrl();
    }

    @Nullable
    @Override
    public String getName() {
        return getUrl();
    }

    @Nullable
    @Override
    public ItemPresentation getPresentation() {
        return new InterfaceXItemPresentation();
    }

    @Override
    public void navigate(boolean requestFocus) {
        if (navigationElement != null) {
            navigationElement.navigate(requestFocus);
        }
    }

    @Override
    public boolean canNavigate() {
        return navigationElement.canNavigate();
    }

    @Override
    public boolean canNavigateToSource() {
        return true;
    }

    public boolean matches(String queryText) {
        String pattern = queryText;
        if (pattern.equals("/")) {
            return true;
        }

        com.intellij.psi.codeStyle.MinusculeMatcher matcher =
                com.intellij.psi.codeStyle.NameUtil.buildMatcher("*" + pattern, com.intellij.psi.codeStyle.NameUtil.MatchingCaseSensitivity.NONE
        );

        return matcher.matches(getUrl());
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public PsiMethod getPsiMethod() {
        return psiMethod;
    }

    public void setPsiMethod(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public void setMethod(HttpMethod method) {
        this.method = method;
    }


    public String getFullUrl() {
        if (module == null) {
            return getUrl();
        }

        ModuleHelper moduleHelper = ModuleHelper.create(module);
        return moduleHelper.getServiceHostPrefix() + originalItem.getUrl();
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public String getKey() {
        return this.module.getName() + this.getFullUrl() + this.getMethod();
    }

    private class InterfaceXItemPresentation implements ItemPresentation {

        @Nullable
        @Override
        public String getPresentableText() {
            return originalItem.getUrl();
        }

        @Nullable
        @Override
        public String getLocationString() {

            // 2023.3 Threading Model Changes
            // https://plugins.jetbrains.com/docs/intellij/general-threading-rules.html#-9rmqiu_24
            return ApplicationManager.getApplication()
                    .runWriteAction(new Computable<>() {
                        String location = null;
                        @Override
                        public String compute() {
                            return null;
                        }

                        @Override
                        public String get() {
                            String fileName = psiElement.getContainingFile().getName();
                            if (psiElement instanceof PsiMethod) {
                                PsiMethod psiMethod = ((PsiMethod) psiElement);
                                if (module != null) {
                                    location = module.getName() + "#" + psiMethod.getContainingClass().getName().concat("#").concat(psiMethod.getName());
                                } else {
                                    location = psiMethod.getContainingClass().getName().concat("#").concat(psiMethod.getName());
                                }
                            }
                            return "(" + location + ")";
                        }
                    });
        }

        @Nullable
        @Override
        public Icon getIcon(boolean unused) {
            return ToolkitIcons.METHOD.get(method);
        }
    }
}
