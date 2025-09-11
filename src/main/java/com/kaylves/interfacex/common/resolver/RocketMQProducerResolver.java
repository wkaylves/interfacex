package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.rocketmq.RocketMQProducerAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.method.HttpMethod;
import com.kaylves.interfacex.navigator.RestServiceItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RocketMQProducerResolver extends BaseServiceResolver {

    public RocketMQProducerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(RocketMQProducerAnnotation.ClassAnnotation.getShortName(), project, globalSearchScope);

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


                PsiAnnotation psiMethodAnnotation = psiMethod.getAnnotation(RocketMQProducerAnnotation.PATH.getQualifiedName());

                if (psiMethodAnnotation == null) {
                    continue;
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

                        RestServiceItem item = new RestServiceItem(psiMethod, requestMethod, path, false);
                        itemList.add(item);
                    }
                }


            }
        }

        return itemList;
    }

    @Override
    public String getServiceItem() {
        return "RocketMQ-Producer";
    }
}
