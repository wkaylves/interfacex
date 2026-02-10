package com.kaylves.interfacex.module.rocketmq.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.module.rocketmq.RocketMQItem;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RocketMQTemplateProducerResolver extends BaseServiceResolver {

    public RocketMQTemplateProducerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        return findProducerCalls(project, this.module);
    }

    private List<InterfaceXItem> findProducerCalls(Project project, Module module) {

        List<InterfaceXItem> results = new ArrayList<>();

        String[] targetClassNames = {
                "org.apache.rocketmq.spring.core.RocketMQTemplate"
        };

        String rabbitMethodName = "convertAndSend";

        GlobalSearchScope scope = GlobalSearchScope.everythingScope(project);

        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);

        for (String className : targetClassNames) {

            PsiClass psiClass = IdeaPluginUtils.findPsiClass(project,className, scope);

            if (psiClass == null){
                continue;
            }

            PsiMethod[] psiMethods = psiClass.findMethodsByName(rabbitMethodName, true);

            for (PsiMethod method : psiMethods) {

                Collection<PsiReference> references = ReferencesSearch.search(method, moduleScope).findAll();
                for (PsiReference ref : references) {
                    PsiElement element = ref.getElement(); // ✅ 正确获取 PSI 元素

                    if (element instanceof PsiReferenceExpression) {
                        PsiElement parent = element.getParent();

                        if (parent instanceof PsiMethodCallExpression callExpression) {

                            addCall(results, callExpression, rabbitMethodName);
                        }
                    }
                }
            }
        }

        return results;
    }

    private void addCall(List<InterfaceXItem> results, PsiMethodCallExpression callExpr, String callType) {

        PsiElement parent = PsiTreeUtil.getParentOfType(callExpr, PsiMethod.class);

        if (!(parent instanceof PsiMethod method)) {
            return;
        }

        PsiElement psiMethodParent = method.getParent();

        if(psiMethodParent instanceof PsiClass psiClass){
            //过滤测试包类文件
            if(PsiAnnotationHelper.isTestPackage(psiClass)){
                return;
            }
        }

        String requestMethod = HttpMethod.PRODUCE.name();

        RocketMQItem rocketMQItem = RocketMQItem.builder().tag(method.getName()).build();

        results.add(new InterfaceXItem(method, InterfaceXItemCategoryEnum.RocketMQProducer, requestMethod, rocketMQItem, false));
    }

    @Override
    public String getServiceItemCategory() {
        return "RocketMQTemplate-Producer";
    }
}
