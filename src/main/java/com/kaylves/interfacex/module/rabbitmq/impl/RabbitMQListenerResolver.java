package com.kaylves.interfacex.module.rabbitmq.impl;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.module.rabbitmq.RabbitMQAnnotation;
import com.kaylves.interfacex.module.rabbitmq.RabbitMQItem;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
public class RabbitMQListenerResolver extends BaseServiceResolver {

    public RabbitMQListenerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceXItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RabbitMQAnnotation.PATH.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {

            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if(psiElement instanceof PsiMethod psiMethod) {

                PsiElement psiMethodParent = psiMethod.getParent();

                if(psiMethodParent instanceof PsiClass psiClass){
                    //过滤测试包类文件
                    if(PsiAnnotationHelper.isTestPackage(psiClass)){
                        continue;
                    }
                }

                String requestMethod = HttpMethod.CONSUME.name();

                final String queue = StringUtils.trimToEmpty(PsiAnnotationHelper.getOneQueue(psiAnnotation));

                String path = MessageFormat.format("{0}",queue);

                RabbitMQItem rabbitMQItem = RabbitMQItem.builder()
                        .queueName(path)
                        .build();

                InterfaceXItem item = new InterfaceXItem(psiMethod, InterfaceXItemCategoryEnum.RabbitMQListener,requestMethod, rabbitMQItem, false);
                itemList.add(item);
            }
        }

        return itemList;
    }

    @Override
    public String getServiceItemCategory() {
        return "RabbitMQ-Listener";
    }
}
