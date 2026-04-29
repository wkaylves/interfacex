package com.kaylves.interfacex.finder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.kaylves.interfacex.module.http.springmvc.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.module.http.springmvc.SpringRequestMethodAnnotation;
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
        String httpMethod = getHttpMethod(controllerMethod);

        log.debug("Finding Feign clients for Controller: path={}, httpMethod={}", controllerMethodPath, httpMethod);

        Project project = controllerMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        Collection<PsiAnnotation> feignAnnotations = JavaAnnotationIndex.getInstance()
            .get(SpringHttpRequestAnnotation.FEIGN_CLIENT.getShortName(), project, searchScope);

        for (PsiAnnotation annotation : feignAnnotations) {
            PsiClass feignInterface = getClassFromAnnotation(annotation);
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

        String classPath = getClassPath(containingClass);
        String methodPath = getMethodMappingPath(method);

        return mergePath(classPath, methodPath);
    }

    private static String getClassPath(PsiClass psiClass) {
        PsiAnnotation requestMapping = psiClass.getAnnotation("org.springframework.web.bind.annotation.RequestMapping");
        if (requestMapping != null) {
            String path = PsiAnnotationHelper.getAnnotationAttributeValue(requestMapping, "value");
            if (StringUtils.isNotBlank(path)) {
                return path;
            }
            return PsiAnnotationHelper.getAnnotationAttributeValue(requestMapping, "path");
        }

        for (String annotationName : new String[]{"GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping"}) {
            PsiAnnotation annotation = psiClass.getAnnotation("org.springframework.web.bind.annotation." + annotationName);
            if (annotation != null) {
                String path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");
                if (StringUtils.isNotBlank(path)) {
                    return path;
                }
                return PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "path");
            }
        }

        return "";
    }

    private static String getMethodMappingPath(PsiMethod method) {
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) {
                continue;
            }

            for (SpringRequestMethodAnnotation mappingAnnotation : SpringRequestMethodAnnotation.values()) {
                if (qualifiedName.endsWith(mappingAnnotation.name()) ||
                    qualifiedName.equals(mappingAnnotation.getQualifiedName())) {
                    String path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");
                    if (StringUtils.isBlank(path)) {
                        path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "path");
                    }
                    return path != null ? path : "";
                }
            }
        }
        return "";
    }

    private static String getHttpMethod(PsiMethod method) {
        PsiAnnotation[] annotations = method.getModifierList().getAnnotations();
        for (PsiAnnotation annotation : annotations) {
            String qualifiedName = annotation.getQualifiedName();
            if (qualifiedName == null) {
                continue;
            }

            for (SpringRequestMethodAnnotation mappingAnnotation : SpringRequestMethodAnnotation.values()) {
                if (qualifiedName.endsWith(mappingAnnotation.name()) ||
                    qualifiedName.equals(mappingAnnotation.getQualifiedName())) {
                    return mappingAnnotation.methodName();
                }
            }
        }
        return null;
    }

    private static PsiClass getClassFromAnnotation(PsiAnnotation annotation) {
        PsiElement parent = annotation.getParent();
        if (parent instanceof PsiModifierList) {
            PsiElement grandparent = parent.getParent();
            if (grandparent instanceof PsiClass) {
                return (PsiClass) grandparent;
            }
        }
        return null;
    }

    private static String mergePath(String basePath, String methodPath) {
        StringBuilder result = new StringBuilder();
        if (StringUtils.isNotBlank(basePath)) {
            result.append(basePath);
            if (!basePath.endsWith("/")) {
                result.append("/");
            }
        }
        if (StringUtils.isNotBlank(methodPath)) {
            if (methodPath.startsWith("/")) {
                result.append(methodPath.substring(1));
            } else {
                result.append(methodPath);
            }
        }
        return result.toString();
    }

    private static List<PsiMethod> findMatchingFeignMethods(PsiClass feignInterface, String targetPath, String targetHttpMethod) {
        List<PsiMethod> result = new ArrayList<>();
        PsiMethod[] methods = feignInterface.getMethods();

        String feignPath = getFeignClientPath(feignInterface);

        for (PsiMethod method : methods) {
            String methodPath = getMethodMappingPath(method);
            String fullPath = mergePath(feignPath, methodPath);
            String feignHttpMethod = getHttpMethod(method);

            if (pathMatches(targetPath, fullPath) && httpMethodMatches(targetHttpMethod, feignHttpMethod)) {
                result.add(method);
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

    private static boolean pathMatches(String path1, String path2) {
        if (StringUtils.isBlank(path1) && StringUtils.isBlank(path2)) {
            return true;
        }
        if (StringUtils.isBlank(path1) || StringUtils.isBlank(path2)) {
            return false;
        }

        String normalizedPath1 = normalizePath(path1);
        String normalizedPath2 = normalizePath(path2);

        return normalizedPath1.equals(normalizedPath2);
    }

    private static String normalizePath(String path) {
        String result = path;
        if (!result.startsWith("/")) {
            result = "/" + result;
        }
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        result = result.replaceAll("/+", "/");
        result = result.replaceAll("\\{[^}]+\\}", "\\{*\\}");

        return result;
    }

    private static boolean httpMethodMatches(String method1, String method2) {
        if (method1 == null || method2 == null) {
            return true;
        }
        return method1.equalsIgnoreCase(method2);
    }
}