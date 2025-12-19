package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.rocketmq.RocketMQProducerAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.method.HttpMethod;
import com.kaylves.interfacex.ui.navigator.RestServiceItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RocketMQCustomProducerResolver extends BaseServiceResolver {

    public RocketMQCustomProducerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RocketMQProducerAnnotation.ClassAnnotation.getShortName(), project, globalSearchScope);

        RocketMQProducerAnnotation[] pathArray = RocketMQProducerAnnotation.getPathArray();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if(PsiAnnotationHelper.isTestPackage(psiClass)){
                continue;
            }

            PsiMethod[] psiMethods = psiClass.getMethods();

            for (PsiMethod psiMethod : psiMethods) {

                for (RocketMQProducerAnnotation pathAnnotation : pathArray) {

                    processElement(psiMethod, pathAnnotation, itemList);
                }

            }
        }

        return itemList;
    }

    private static void processElement(PsiMethod psiMethod, RocketMQProducerAnnotation pathAnnotation, List<RestServiceItem> itemList) {
        PsiAnnotation psiMethodAnnotation = psiMethod.getAnnotation(pathAnnotation.getQualifiedName());

        if (psiMethodAnnotation == null) {
            return;
        }

        PsiAnnotationMemberValue nestedAnnotationValue = psiMethodAnnotation.findAttributeValue("rocketmqAttrbute");

        if (nestedAnnotationValue instanceof PsiAnnotation nestedAnnotation) {

            String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                    nestedAnnotation,
                    "topic"));

            String tags = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                    nestedAnnotation,
                    "tag"));

            if (StringUtils.isNotBlank(topic)) {

                String requestMethod = HttpMethod.PRODUCE.name();

                String path = MessageFormat.format("{0}",tags);

                RestServiceItem item = new RestServiceItem(psiMethod, InterfaceXEnum.RocketMQProducer, requestMethod, path, false);
                itemList.add(item);
            }
        }
    }

    @Override
    public String getServiceItem() {
        return "RocketMQ-Producer";
    }
}
