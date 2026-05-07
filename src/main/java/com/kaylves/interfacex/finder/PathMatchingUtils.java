package com.kaylves.interfacex.finder;

import com.intellij.psi.*;
import com.kaylves.interfacex.module.http.springmvc.SpringRequestMethodAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import org.apache.commons.lang3.StringUtils;

public final class PathMatchingUtils {

    private PathMatchingUtils() {
    }

    public static String getHttpMethod(PsiMethod method) {
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

    public static String mergePath(String basePath, String methodPath) {
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

    public static PsiClass getClassFromAnnotation(PsiAnnotation annotation) {
        PsiElement parent = annotation.getParent();
        if (parent instanceof PsiModifierList) {
            PsiElement grandparent = parent.getParent();
            if (grandparent instanceof PsiClass) {
                return (PsiClass) grandparent;
            }
        }
        return null;
    }

    public static boolean pathMatches(String path1, String path2) {
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

    public static String normalizePath(String path) {
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

    public static boolean httpMethodMatches(String method1, String method2) {
        if (method1 == null || method2 == null) {
            return true;
        }
        return method1.equalsIgnoreCase(method2);
    }

    public static String getClassPath(PsiClass psiClass) {
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

    public static String getMethodMappingPath(PsiMethod method) {
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
}