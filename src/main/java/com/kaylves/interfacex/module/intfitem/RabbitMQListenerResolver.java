package com.kaylves.interfacex.module.intfitem;

import com.kaylves.interfacex.common.annotations.InterfaceXEnum;
import com.kaylves.interfacex.common.annotations.rabbitmq.RabbitMQAnnotation;
import com.kaylves.interfacex.common.method.HttpMethod;
import com.kaylves.interfacex.module.navigator.RestServiceItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.*;

@Slf4j
public class RabbitMQListenerResolver extends BaseServiceResolver {

    public RabbitMQListenerResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
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

                RestServiceItem item = new RestServiceItem(psiMethod, requestMethod, path, false);
                itemList.add(item);
            }
        }

        return itemList;
    }

    @Override
    public String getServiceItem() {
        return InterfaceXEnum.RabbitMQListener.name();
    }
}
