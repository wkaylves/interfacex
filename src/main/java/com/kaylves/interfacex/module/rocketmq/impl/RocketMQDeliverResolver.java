package com.kaylves.interfacex.module.rocketmq.impl;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.module.rocketmq.RocketMQDeliverAnnotation;
import com.kaylves.interfacex.module.rocketmq.RocketMQItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RocketMQDeliverResolver extends BaseServiceResolver {

    public RocketMQDeliverResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceXItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RocketMQDeliverAnnotation.PATH.getShortName(), project, globalSearchScope);

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

                String requestMethod = HttpMethod.PRODUCE.name();

                String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiAnnotation,
                        "topic"));
                String tags = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiAnnotation,
                        "tags"));
                String keys = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(
                        psiAnnotation,
                        "keys"));

                RocketMQItem rocketMQItem = RocketMQItem.builder().tag(tags).topic(topic).keys(keys).build();

                InterfaceXItem item = new InterfaceXItem(psiMethod, InterfaceXItemCategoryEnum.RocketMQDeliver, requestMethod, rocketMQItem, false);
                itemList.add(item);
            }
        }

        return itemList;
    }

    @Override
    public String getServiceItemCategory() {
        return "RocketMQ-Deliver";
    }
}
