package com.kaylves.interfacex.annotations.rocketmq;

import com.kaylves.interfacex.annotations.http.JakartaPathAnnotation;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * RocketMQAnnotationHelper
 */
public class RocketMQAnnotationHelper {

    public static RequestPath[] getRequestPaths(PsiMethod psiMethod) {

        List<RequestPath> list = new ArrayList<>();

        PsiAnnotation psiAnnotation = psiMethod.getModifierList().findAnnotation(RocketMQAnnotation.RocketMQMessageListener.getQualifiedName());

        String consumerGroup = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "consumerGroup"));
        String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "topic"));
        String selectorExpression = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "selectorExpression"));

        String path2 = MessageFormat.format("consumerGroup:{0}_topic:{1}_selectorExpre:{2}",consumerGroup,topic,selectorExpression);

        list.add(new RequestPath(path2, "consume"));

        return list.toArray(new RequestPath[0]);
    }

    public static RequestPath[] getRequestPaths(PsiClass psiClass) {

        List<RequestPath> list = new ArrayList<>();

        PsiAnnotation psiAnnotation = psiClass.getModifierList().findAnnotation(RocketMQAnnotation.RocketMQMessageListener.getQualifiedName());

        String consumerGroup = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "consumerGroup"));
        String topic = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "topic"));
        String selectorExpression = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "selectorExpression"));

        String path = MessageFormat.format("{0}_{1}",psiClass.getName(),topic);

        list.add(new RequestPath(path, "consume"));

        return list.toArray(new RequestPath[0]);
    }

    public static String getClassUriPath(PsiClass psiClass) {
        PsiAnnotation annotation = Objects.requireNonNull(psiClass.getModifierList()).findAnnotation(JakartaPathAnnotation.PATH.getQualifiedName());
        return StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "topic"));
    }

}
