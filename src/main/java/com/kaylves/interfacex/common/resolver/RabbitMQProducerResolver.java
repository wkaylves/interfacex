package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.method.HttpMethod;
import com.kaylves.interfacex.navigator.RestServiceItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RabbitMQProducerResolver extends BaseServiceResolver {

    public RabbitMQProducerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        return findProducerCalls(project, this.module);
    }

    private List<RestServiceItem> findProducerCalls(Project project, Module module) {
        List<RestServiceItem> results = new ArrayList<>();
        String rabbitMethodName = "convertAndSend";
        String[] targetClassNames = {
                "com.hbfintech.mint.rabbit.core.RabbitTemplateProxy",
                "org.springframework.amqp.rabbit.core.RabbitTemplate",
                "org.springframework.amqp.core.AmqpTemplate"
        };

        GlobalSearchScope scope = GlobalSearchScope.everythingScope(project);

        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);

        for (String className : targetClassNames) {
            PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, scope);
            if (psiClass == null) continue;

            for (PsiMethod method : psiClass.findMethodsByName(rabbitMethodName, false)) {
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

    private void addCall(List<RestServiceItem> results, PsiMethodCallExpression callExpr, String callType) {

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

        results.add(new RestServiceItem(method, InterfaceXEnum.RabbitMQProducer, requestMethod, method.getName(), false));

    }

    @Override
    public String getServiceItem() {
        return "RabbitMQ-Producer";
    }
}
