package com.kaylves.interfacex.strategy;

import com.kaylves.interfacex.annotations.rocketmq.RocketMQAnnotation;
import com.kaylves.interfacex.bean.RocketMQProducerExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiModifierList;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang.StringUtils;

import java.util.*;

public class RocketMQListenerStrategy implements ServiceStrategy<RocketMQProducerExportBean> {

    @Override
    public List<RocketMQProducerExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<RocketMQProducerExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {

            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            List<RocketMQProducerExportBean> callList = ReadAction.compute(() -> filter(project, module, globalSearchScope));

            serviceExportBeans.addAll(callList);

        });
        return serviceExportBeans;
    }

    public List<RocketMQProducerExportBean> filter(Project project, Module module, GlobalSearchScope globalSearchScope) {
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RocketMQAnnotation.RocketMQMessageListener.getShortName(), project, globalSearchScope);

        List<RocketMQProducerExportBean> serviceExportBeans = new ArrayList<>();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {

            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                continue;
            }

            String consumerGroup = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "consumerGroup"));
            String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "topic"));
            String selectorExpression = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "selectorExpression"));

            serviceExportBeans.add(RocketMQProducerExportBean.builder()
                    .modelName(module.getName())
                    .interfaceType("RocketMQListener")
                    .topic(topic)
                    .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                    .tag(selectorExpression)
                    .consumerGroup(consumerGroup)
                    .desc(IdeaPluginUtils.obtainDocAsString(psiClass.getDocComment()))
                    .simpleClassName(psiClass.getName())
                    .fullClassName(psiClass.getQualifiedName())
                    .build());
        }

        return serviceExportBeans;
    }


}
