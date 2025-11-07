package com.kaylves.interfacex.strategy;

import com.kaylves.interfacex.annotations.rocketmq.RocketMQDeliverAnnotation;
import com.kaylves.interfacex.annotations.spring.SpringComponentAnnotation;
import com.kaylves.interfacex.bean.RocketMQProducerExportBean;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class RocketMQDeliverServiceStrategy implements ServiceStrategy<RocketMQProducerExportBean> {

    @Override
    public List<RocketMQProducerExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<RocketMQProducerExportBean> rocketMQProducerExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {
            Collection<PsiAnnotation> psiAnnotations = new ArrayList<>();
            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            List<RocketMQProducerExportBean> callList = ReadAction.compute(() -> filters(project, module, psiAnnotations, globalSearchScope));
            rocketMQProducerExportBeans.addAll(callList);

        });

        return rocketMQProducerExportBeans;
    }

    public static List<RocketMQProducerExportBean> filters(Project project, Module module, Collection<PsiAnnotation> psiAnnotations, GlobalSearchScope globalSearchScope) {


        Arrays.stream(SpringComponentAnnotation.values()).forEach(springComponentAnnotation -> {
            Collection<PsiAnnotation> psiAnnotationCollection = JavaAnnotationIndex.getInstance().get(springComponentAnnotation.getShortName(), project, globalSearchScope);
            if (!psiAnnotationCollection.isEmpty()) {
                psiAnnotations.addAll(psiAnnotationCollection);
            }
        });

        List<RocketMQProducerExportBean> rocketMQProducerExportBeans = new ArrayList<>();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                continue;
            }

            PsiMethod[] psiMethods = psiClass.getMethods();

            for (PsiMethod psiMethod : psiMethods) {

                PsiAnnotation psiMethodAnnotation = psiMethod.getAnnotation(RocketMQDeliverAnnotation.PATH.getQualifiedName());

                if (psiMethodAnnotation == null) {
                    continue;
                }

                String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiMethodAnnotation,
                        "topic"));
                String tags = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiMethodAnnotation,
                        "tags"));
                String keys = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiMethodAnnotation,
                        "keys"));

                String path = MessageFormat.format("topic:{0}_tags:{1}_keys:{2}", topic, tags, keys);


                if (StringUtils.isNotBlank(topic)) {
                    rocketMQProducerExportBeans.add(RocketMQProducerExportBean.builder()
                            .modelName(module.getName())
                            .interfaceType("RocketMQ_Deliver")
                            .topic(topic)
                            .tag(tags)
                            .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                            .desc(IdeaPluginUtils.obtainDocAsString(psiMethod))
                            .simpleClassName(psiClass.getName())
                            .fullClassName(psiClass.getQualifiedName())
                            .build());
                }

            }
        }
        return rocketMQProducerExportBeans;
    }

}
