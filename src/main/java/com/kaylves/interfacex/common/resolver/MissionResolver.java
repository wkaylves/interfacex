package com.kaylves.interfacex.common.resolver;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.mission.MissionClientAnnotation;
import com.kaylves.interfacex.annotations.mission.MissionClientMethodAnnotation;
import com.kaylves.interfacex.method.RequestPath;
import com.kaylves.interfacex.navigator.RestServiceItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class MissionResolver extends BaseServiceResolver {

    /**
     * 构造函数，用于初始化任务解析器
     *
     * @param module 模块对象，用于初始化任务解析器的模块属性
     */
    public MissionResolver(Module module) {
        // 使用传入的module参数初始化对象的module属性
        this.module = module;
    }

    @Override
    public List<RestServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<RestServiceItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(MissionClientAnnotation.MISSION_CLIENT_ANNOTATION.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {

            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            //过滤测试包类文件
            if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                continue;
            }

            itemList.addAll(getServiceItemList(psiClass));
        }

        return itemList;
    }

    protected List<RestServiceItem> getServiceItemList(PsiClass psiClass) {
        PsiMethod[] psiMethods = psiClass.getMethods();

        List<RestServiceItem> itemList = new ArrayList<>();

        for (PsiMethod psiMethod : psiMethods) {

            PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                for (MissionClientMethodAnnotation mappingAnnotation : MissionClientMethodAnnotation.values()) {

                    if (mappingAnnotation.getQualifiedName().equals(annotation.getQualifiedName())) {
                        PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path").forEach(path -> itemList.add(createRestServiceItem(psiMethod, InterfaceXEnum.Mission,"", new RequestPath(path, "POST"))));
                    }
                }
            }
        }
        return itemList;
    }

    @Override
    public String getServiceItem() {
        return "MissionClient";
    }
}
