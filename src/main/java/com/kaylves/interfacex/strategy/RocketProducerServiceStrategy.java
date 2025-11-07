package com.kaylves.interfacex.strategy;

import com.kaylves.interfacex.annotations.rocketmq.RocketMQProducerAnnotation;
import com.kaylves.interfacex.bean.RocketMQProducerExportBean;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * RocketMQ服务暴露策略
 */
public class RocketProducerServiceStrategy implements ServiceStrategy<RocketMQProducerExportBean> {
    @Override
    public List<RocketMQProducerExportBean> obtainServiceExportBeans(Project project) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<RocketMQProducerExportBean> serviceExportBeans = new ArrayList<>();

        Arrays.stream(modules).forEach(module -> {
            Collection<PsiAnnotation> psiAnnotations = new ArrayList<>();
            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            List<RocketMQProducerExportBean> callList = ReadAction.compute(() -> filters(project, module, psiAnnotations, globalSearchScope));
            serviceExportBeans.addAll(callList);
        });

        return serviceExportBeans;
    }

    public static List<RocketMQProducerExportBean> filters(Project project,Module module, Collection<PsiAnnotation> psiAnnotations, GlobalSearchScope globalSearchScope) {

        List<RocketMQProducerExportBean> serviceExportBeans = new ArrayList<>();

        Collection<PsiAnnotation> psiAnnotationCollection = JavaAnnotationIndex.getInstance().get(RocketMQProducerAnnotation.ClassAnnotation.getShortName(), project, globalSearchScope);

        if (!psiAnnotationCollection.isEmpty()) {
            psiAnnotations.addAll(psiAnnotationCollection);
        }

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


                RocketMQProducerAnnotation[] pathArray = RocketMQProducerAnnotation.getPathArray();
                for (RocketMQProducerAnnotation pathAnnotation : pathArray) {
                    PsiAnnotation psiMethodAnnotation = psiMethod.getAnnotation(pathAnnotation.getQualifiedName());

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

                        Boolean result = ReferencesSearch.search(psiMethod,globalSearchScope).findFirst()!=null;


                        if (StringUtils.isNotBlank(topic)) {

                            serviceExportBeans.add(RocketMQProducerExportBean.builder()
                                    .modelName(module.getName())
                                    .interfaceType("RocketMQProducer")
                                    .topic(topic)
                                    .auth(IdeaPluginUtils.obtainAuth(psiClass.getDocComment()))
                                    .tag(tags)
                                    .desc(IdeaPluginUtils.obtainDocAsString(psiMethod))
                                    .simpleClassName(psiClass.getName())
                                    .usedAble(result)
                                    .fullClassName(psiClass.getQualifiedName())
                                    .build());
                        }

                    }
                }




            }
        }

        return serviceExportBeans;

    }
}
