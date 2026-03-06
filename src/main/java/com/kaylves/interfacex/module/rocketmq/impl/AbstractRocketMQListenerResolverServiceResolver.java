package com.kaylves.interfacex.module.rocketmq.impl;

import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.module.rocketmq.RocketMQAnnotation;
import com.kaylves.interfacex.module.rocketmq.RocketMQItem;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

/**
 * @author kaylves
 */
@Slf4j
public abstract class AbstractRocketMQListenerResolverServiceResolver extends BaseServiceResolver {


    @Override
    public List<InterfaceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceItem> itemList = new ArrayList<>();

        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(getRocketMQAnnotation().getShortName(),project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();

            if (!(psiElement instanceof PsiClass psiClass)) {
                continue;
            }

            Optional<PsiMethod> list = Arrays.stream(psiClass.getMethods()).filter(psiMethod -> psiMethod.getName().equals("consume")).findFirst();

            list.ifPresent(psiMethod -> {
                String requestMethod = HttpMethod.CONSUME.name();

                String selectorExpression = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "selectorExpression"));

                String path = MessageFormat.format("{0}",selectorExpression);

                RocketMQItem rocketMQItem = RocketMQItem.builder().tag(path).build();

                InterfaceItem item = new InterfaceItem(psiMethod, InterfaceItemCategoryEnum.RocketMQListener, requestMethod, rocketMQItem, false);
                item.setUseAble(IdeaPluginUtils.getUseAbleOnClassOrMethod(psiClass, psiMethod));
                itemList.add(item);
            });

        }

        return itemList;
    }


    public abstract RocketMQAnnotation getRocketMQAnnotation();

    public abstract String getServiceItemCategory();
}
