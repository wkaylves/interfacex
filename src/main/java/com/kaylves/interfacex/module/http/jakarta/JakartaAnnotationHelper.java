package com.kaylves.interfacex.module.http.jakarta;

import com.kaylves.interfacex.module.http.JakartaHttpMethodAnnotation;
import com.kaylves.interfacex.module.http.JakartaPathAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import com.kaylves.interfacex.module.http.method.RequestPath;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class JakartaAnnotationHelper {

    private static String getWsPathValue(PsiAnnotation annotation) {
        String value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");

        return value != null ? value : "";
    }

    public static RequestPath[] getRequestPaths(PsiMethod psiMethod) {
        PsiAnnotation[] annotations = psiMethod.getModifierList().getAnnotations();
        List<RequestPath> list = new ArrayList<>();

        PsiAnnotation wsPathAnnotation = psiMethod.getModifierList().findAnnotation(JakartaPathAnnotation.PATH.getQualifiedName());
        String path = wsPathAnnotation == null ? psiMethod.getName() : getWsPathValue(wsPathAnnotation);

        JakartaHttpMethodAnnotation[] JakartaHttpMethodAnnotations = JakartaHttpMethodAnnotation.values();
        Arrays.stream(annotations).forEach(a -> Arrays.stream(JakartaHttpMethodAnnotations).forEach(methodAnnotation -> {
            if (Objects.equals(a.getQualifiedName(), methodAnnotation.getQualifiedName())) {
                list.add(new RequestPath(path, methodAnnotation.getShortName()));
            }
        }));

        return list.toArray(new RequestPath[0]);
    }

    public static String getClassUriPath(PsiClass psiClass) {
        PsiAnnotation annotation = Objects.requireNonNull(psiClass.getModifierList()).findAnnotation(JakartaPathAnnotation.PATH.getQualifiedName());
        String path = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");
        return path != null ? path : "";
    }

    public static String getMethodUriPath(PsiMethod psiMethod) {
        JakartaHttpMethodAnnotation requestAnnotation = null;

        List<JakartaHttpMethodAnnotation> httpMethodAnnotations = Arrays.stream(JakartaHttpMethodAnnotation.values()).filter(annotation -> psiMethod.getModifierList().findAnnotation(annotation.getQualifiedName()) != null).collect(Collectors.toList());

        if (!httpMethodAnnotations.isEmpty()) {
            requestAnnotation = httpMethodAnnotations.get(0);
        }

        String mappingPath;
        if (requestAnnotation != null) {
            PsiAnnotation annotation = psiMethod.getModifierList().findAnnotation(JakartaPathAnnotation.PATH.getQualifiedName());
            mappingPath = getWsPathValue(annotation);
        } else {
            String methodName = psiMethod.getName();
            mappingPath = StringUtils.uncapitalize(methodName);
        }
        return mappingPath;
    }
}
