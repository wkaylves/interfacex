package com.kaylves.interfacex.module.rabbitmq.impl;

import com.kaylves.interfacex.bean.RabbitMQProducerExportBean;
import com.kaylves.interfacex.common.ExportServiceStrategy;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * RocketMQ服务暴露策略
 */
@Slf4j
public class RabbitMQProducerExportServiceStrategy implements ExportServiceStrategy<RabbitMQProducerExportBean> {

    @Override
    public List<RabbitMQProducerExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<RabbitMQProducerExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {

            List<RabbitMQProducerExportBean> callList =  ReadAction.compute(() -> findProducerCalls(project, module));

            serviceExportBeans.addAll(callList);

        });

        return serviceExportBeans;
    }

    private List<RabbitMQProducerExportBean> findProducerCalls(Project project, Module module) {
        List<RabbitMQProducerExportBean> results = new ArrayList<>();

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
                            addCall(module,results, callExpression, rabbitMethodName);
                        }
                    }
                }
            }
        }

        return results;
    }

    private void addCall(Module module, List<RabbitMQProducerExportBean> results, PsiMethodCallExpression callExpr, String callType) {
        PsiElement parent = PsiTreeUtil.getParentOfType(callExpr, PsiMethod.class);

        if (!(parent instanceof PsiMethod method)) {
            return;
        }

        PsiClass psiClass = method.getContainingClass();
        if (psiClass == null) return;

        int lineNumber = callExpr.getContainingFile().getViewProvider().getDocument()
                .getLineNumber(callExpr.getTextOffset()) + 1;


        RabbitMQProducerExportBean rabbitMQProducerExportBean = RabbitMQProducerExportBean.builder()
                .modelName(module.getName())
                .interfaceType("RabbitMQ_Producer")
                .methodName(method.getName())
                .lineNumber(lineNumber)
                .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                .desc(IdeaPluginUtils.obtainDocAsString(method))
                .simpleClassName(psiClass.getName())
                .fullClassName(psiClass.getQualifiedName())
                .build();

        results.add(rabbitMQProducerExportBean);

    }

}
