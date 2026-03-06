package com.kaylves.interfacex.module.mission;

import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.module.http.method.RequestPath;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.common.InterfaceItem;
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
public class MissionResolverServiceResolver extends BaseServiceResolver {

    /**
     * 构造函数，用于初始化任务解析器
     *
     * @param module 模块对象，用于初始化任务解析器的模块属性
     */
    public MissionResolverServiceResolver(Module module) {
        // 使用传入的module参数初始化对象的module属性
        this.module = module;
    }

    @Override
    public List<InterfaceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceItem> itemList = new ArrayList<>();
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

    protected List<InterfaceItem> getServiceItemList(PsiClass psiClass) {
        PsiMethod[] psiMethods = psiClass.getMethods();

        List<InterfaceItem> itemList = new ArrayList<>();

        for (PsiMethod psiMethod : psiMethods) {

            PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();

            for (PsiAnnotation annotation : annotations) {
                for (MissionClientMethodAnnotation mappingAnnotation : MissionClientMethodAnnotation.values()) {

                    if (mappingAnnotation.getQualifiedName().equals(annotation.getQualifiedName())) {
                        PsiAnnotationHelper.getAnnotationAttributeValues(annotation, "path").forEach(path -> itemList.add(createRestServiceItem(psiMethod, InterfaceItemCategoryEnum.Mission,"", new RequestPath(path, "POST"))));
                    }
                }
            }
        }
        return itemList;
    }

    @Override
    public String getServiceItemCategory() {
        return "MissionClient";
    }
}
