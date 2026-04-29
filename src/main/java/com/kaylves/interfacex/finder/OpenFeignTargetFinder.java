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
        String feignMethodPath = getMethodPath(feignMethod);
        String fullPath = mergePath(feignPath, feignMethodPath);
        String httpMethod = getHttpMethod(feignMethod);

        log.debug("Finding controller for Feign: path={}, method={}, httpMethod={}", fullPath, feignMethodPath, httpMethod);

        Project project = feignMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        for (String annotationName : CONTROLLER_ANNOTATIONS) {
            Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance()
                .get(annotationName, project, searchScope);

            for (PsiAnnotation annotation : annotations) {
                PsiClass controllerClass = getClassFromAnnotation(annotation);
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

    private static String getMethodPath(PsiMethod method) {
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

    private static List<PsiMethod> findMatchingMethods(PsiClass controllerClass, String targetPath, String targetHttpMethod) {
        List<PsiMethod> result = new ArrayList<>();
        PsiMethod[] methods = controllerClass.getMethods();

        String classPath = getClassPath(controllerClass);

        for (PsiMethod method : methods) {
            String methodPath = getMethodMappingPath(method);
            String fullPath = mergePath(classPath, methodPath);
            String controllerHttpMethod = getHttpMethod(method);

            if (pathMatches(targetPath, fullPath) && httpMethodMatches(targetHttpMethod, controllerHttpMethod)) {
                result.add(method);
            }
        }

        return result;
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