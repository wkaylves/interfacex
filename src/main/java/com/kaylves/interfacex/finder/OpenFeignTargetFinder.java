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
public class OpenFeignTargetFinder {

    private static final String[] CONTROLLER_ANNOTATIONS = {
        "RestController",
        "Controller",
        "RequestMapping"
    };

    public static List<PsiMethod> findTargetControllers(PsiMethod feignMethod) {
        List<PsiMethod> result = new ArrayList<>();

        PsiClass feignInterface = feignMethod.getContainingClass();
        if (feignInterface == null) {
            return result;
        }

        String feignPath = getFeignClientPath(feignInterface);
        String feignMethodPath = PathMatchingUtils.getMethodMappingPath(feignMethod);
        String fullPath = PathMatchingUtils.mergePath(feignPath, feignMethodPath);
        String httpMethod = PathMatchingUtils.getHttpMethod(feignMethod);

        log.debug("Finding controller for Feign: path={}, method={}, httpMethod={}", fullPath, feignMethodPath, httpMethod);

        Project project = feignMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        for (String annotationName : CONTROLLER_ANNOTATIONS) {
            Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance()
                .get(annotationName, project, searchScope);

            for (PsiAnnotation annotation : annotations) {
                PsiClass controllerClass = PathMatchingUtils.getClassFromAnnotation(annotation);
                if (controllerClass == null) {
                    continue;
                }

                if (PsiAnnotationHelper.isTestPackage(controllerClass)) {
                    continue;
                }

                result.addAll(findMatchingMethods(controllerClass, fullPath, httpMethod));
            }
        }

        return result;
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

    private static List<PsiMethod> findMatchingMethods(PsiClass controllerClass, String targetPath, String targetHttpMethod) {
        List<PsiMethod> result = new ArrayList<>();
        PsiMethod[] methods = controllerClass.getMethods();

        String classPath = PathMatchingUtils.getClassPath(controllerClass);

        for (PsiMethod method : methods) {
            String methodPath = PathMatchingUtils.getMethodMappingPath(method);
            String fullPath = PathMatchingUtils.mergePath(classPath, methodPath);
            String controllerHttpMethod = PathMatchingUtils.getHttpMethod(method);

            if (PathMatchingUtils.pathMatches(targetPath, fullPath) && PathMatchingUtils.httpMethodMatches(targetHttpMethod, controllerHttpMethod)) {
                result.add(method);
            }
        }

        return result;
    }
}