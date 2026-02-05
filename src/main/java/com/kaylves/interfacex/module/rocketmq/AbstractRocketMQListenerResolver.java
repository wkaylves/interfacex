package com.kaylves.interfacex.module.rocketmq;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
public abstract class AbstractRocketMQListenerResolver extends BaseServiceResolver {


    @Override
    public List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceXItem> itemList = new ArrayList<>();

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

                InterfaceXItem item = new InterfaceXItem(psiMethod, InterfaceXItemCategoryEnum.RocketMQListener, requestMethod, path, false);
                itemList.add(item);
            });

        }

        return itemList;
    }


    public abstract RocketMQAnnotation  getRocketMQAnnotation();

    public abstract String getServiceItemCategory();
}
