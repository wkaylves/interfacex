package com.kaylves.interfacex.module.intfitem;

import com.kaylves.interfacex.common.annotations.rocketmq.RocketMQListenerSpringAnnotation;
import com.kaylves.interfacex.common.method.HttpMethod;
import com.kaylves.interfacex.module.navigator.RestServiceItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
public abstract class AbstractRocketMQListenerResolver extends BaseServiceResolver {


    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();

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

                RestServiceItem item = new RestServiceItem(psiMethod, requestMethod, path, false);
                itemList.add(item);
            });

        }

        return itemList;
    }


    public abstract RocketMQListenerSpringAnnotation getRocketMQAnnotation();

    public abstract String getServiceItem();
}
