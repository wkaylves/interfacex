package com.kaylves.interfacex.module.xxljob;

import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.module.resolver.BaseServiceResolver;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.common.constants.HttpMethod;
import com.kaylves.interfacex.module.http.method.RequestPath;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @author kaylves
 */
@Slf4j
public class XXLJobResolver extends BaseServiceResolver {

    public XXLJobResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<InterfaceXItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceXItem> itemList = new ArrayList<>();
        itemList.addAll(obtainItemsOnClass(project, globalSearchScope));
        itemList.addAll(obtainItemsOnMethod(project, globalSearchScope));

        return itemList;
    }

    private static List<InterfaceXItem> obtainItemsOnClass(Project project, GlobalSearchScope globalSearchScope) {
        List<InterfaceXItem> itemList = new ArrayList<>();
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(XXLJobComponentAnnotation.JobHandler.getShortName(), project, globalSearchScope);

        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiElement psiElement = obtainPsiElement(psiAnnotation);

            if (!(psiElement instanceof PsiClass psiClass)) {
                continue;
            }

            Optional<PsiMethod> list = Arrays.stream(psiClass.getMethods()).filter(psiMethod -> psiMethod.getName().equals("execute")).findFirst();

            list.ifPresent(psiMethod -> {
                String requestMethod = HttpMethod.EXECUTE.name();
                String consumerGroup = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value"));
                RequestPath methodUriPath = new RequestPath(consumerGroup, "execute");

                XXLJobItem xxlJobItem = XXLJobItem.builder().jobName(methodUriPath.getPath()).build();

                InterfaceXItem item = new InterfaceXItem(psiMethod, InterfaceXItemCategoryEnum.XXLJob, requestMethod, xxlJobItem, false);
                itemList.add(item);
            });

        }

        return itemList;
    }

    private static List<InterfaceXItem> obtainItemsOnMethod(Project project, GlobalSearchScope globalSearchScope) {
        Collection<PsiAnnotation> psiAnnotations = JavaAnnotationIndex.getInstance().get(XXLJobComponentAnnotation.XxlJob.getShortName(), project, globalSearchScope);

        List<InterfaceXItem> itemList = new ArrayList<>();

        for (PsiAnnotation psiAnnotation : psiAnnotations) {

            PsiElement psiElement = obtainPsiElement(psiAnnotation);

            if(psiElement instanceof PsiMethod psiMethod) {

                PsiElement psiMethodParent = psiMethod.getParent();

                if (psiMethodParent instanceof PsiClass psiClass) {
                    //过滤测试包类文件
                    if (PsiAnnotationHelper.isTestPackage(psiClass)) {
                        continue;
                    }
                }

                String requestMethod = HttpMethod.EXECUTE.name();
                String consumerGroup = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value"));
                RequestPath methodUriPath = new RequestPath(consumerGroup, psiMethod.getName());


                XXLJobItem xxlJobItem = XXLJobItem.builder().jobName(methodUriPath.getPath()).build();

                InterfaceXItem item = new InterfaceXItem(psiMethod, InterfaceXItemCategoryEnum.XXLJob,requestMethod, xxlJobItem, false);
                itemList.add(item);
            }

        }

        return itemList;
    }

    private static PsiElement obtainPsiElement(PsiAnnotation psiAnnotation) {
        PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
        return psiModifierList.getParent();
    }

    @Override
    public String getServiceItemCategory() {
        return "XXLJOB";
    }
}
