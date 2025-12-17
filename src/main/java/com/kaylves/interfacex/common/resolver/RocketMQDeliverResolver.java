package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.rocketmq.RocketMQDeliverAnnotation;
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
public class RocketMQDeliverResolver extends BaseServiceResolver {

    public RocketMQDeliverResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
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

                String tags = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "tags"));

                String path = MessageFormat.format("{0}",tags);

                RestServiceItem item = new RestServiceItem(psiMethod, InterfaceXEnum.RocketMQDeliver, requestMethod, path, false);
                itemList.add(item);
            }
        }

        return itemList;
    }

    @Override
    public String getServiceItem() {
        return "RocketMQ-Deliver";
    }
}
