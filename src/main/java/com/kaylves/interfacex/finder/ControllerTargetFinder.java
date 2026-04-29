package com.kaylves.interfacex.finder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.kaylves.interfacex.module.http.springmvc.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class ControllerTargetFinder {

    public static List<PsiMethod> findTargetFeignClients(PsiMethod controllerMethod) {
        List<PsiMethod> result = new ArrayList<>();

        PsiClass controllerClass = controllerMethod.getContainingClass();
        if (controllerClass == null) {
            return result;
        }

        String controllerMethodPath = getMethodPath(controllerMethod);
        String httpMethod = PathMatchingUtils.getHttpMethod(controllerMethod);

        log.debug("Finding Feign clients for Controller: path={}, httpMethod={}", controllerMethodPath, httpMethod);

        Project project = controllerMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        Collection<PsiAnnotation> feignAnnotations = JavaAnnotationIndex.getInstance()
            .get(SpringHttpRequestAnnotation.FEIGN_CLIENT.getShortName(), project, searchScope);

        for (PsiAnnotation annotation : feignAnnotations) {
            PsiClass feignInterface = PathMatchingUtils.getClassFromAnnotation(annotation);
            if (feignInterface == null) {
                continue;
            }

            if (PsiAnnotationHelper.isTestPackage(feignInterface)) {
                continue;
            }

            result.addAll(findMatchingFeignMethods(feignInterface, controllerMethodPath, httpMethod));
        }

        return result;
    }

    private static String getMethodPath(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return "";
        }

        String classPath = PathMatchingUtils.getClassPath(containingClass);
        String methodPath = PathMatchingUtils.getMethodMappingPath(method);

        return PathMatchingUtils.mergePath(classPath, methodPath);
    }

    private static String getFeignClientPath(PsiClass feignInterface) {
        PsiAnnotation feignAnnotation = feignInterface.getAnnotation(SpringHttpRequestAnnotation.FEIGN_CLIENT.getQualifiedName());
        if (feignAnnotation == null) {
            return "";
        }

        String path = PsiAnnotationHelper.getAnnotationAttributeValue(feignAnnotation, "path");
        if (StringUtils.isNotBlank(path)) {
            return path;
        }

        return PsiAnnotationHelper.getAnnotationAttributeValue(feignAnnotation, "value");
    }

    private static List<PsiMethod> findMatchingFeignMethods(PsiClass feignInterface, String targetPath, String targetHttpMethod) {
        List<PsiMethod> result = new ArrayList<>();
        PsiMethod[] methods = feignInterface.getMethods();

        String feignPath = getFeignClientPath(feignInterface);

        for (PsiMethod method : methods) {
            String methodPath = PathMatchingUtils.getMethodMappingPath(method);
            String fullPath = PathMatchingUtils.mergePath(feignPath, methodPath);
            String feignHttpMethod = PathMatchingUtils.getHttpMethod(method);

            if (PathMatchingUtils.pathMatches(targetPath, fullPath) && PathMatchingUtils.httpMethodMatches(targetHttpMethod, feignHttpMethod)) {
                result.add(method);
            }
        }

        return result;
    }
}