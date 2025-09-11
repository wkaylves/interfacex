package com.kaylves.interfacex.utils;

import com.kaylves.interfacex.annotations.http.JaxrsRequestAnnotation;
import com.kaylves.interfacex.annotations.http.SpringControllerAnnotation;
import com.kaylves.interfacex.annotations.http.SpringRequestParamAnnotations;
import com.kaylves.interfacex.common.jaxrs.JaxrsAnnotationHelper;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.Parameter;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PsiMethodHelper {

    PsiMethod psiMethod;
    Project myProject;
    Module myModule;

    protected PsiMethodHelper(PsiMethod psiMethod) {
        this.psiMethod = psiMethod;
    }

    public static PsiMethodHelper create(@NotNull PsiMethod psiMethod) {
        return new PsiMethodHelper(psiMethod);
    }

    public static boolean isSpringRestSupported(PsiClass containingClass) {
        PsiModifierList modifierList = containingClass.getModifierList();
        assert modifierList != null;

        return Arrays.stream(SpringControllerAnnotation.values()).
                anyMatch((springControllerAnnotation) ->
                        modifierList.findAnnotation(springControllerAnnotation.getQualifiedName()) != null);
    }

    public static boolean isJaxrsRestSupported(PsiClass containingClass) {
        PsiModifierList modifierList = containingClass.getModifierList();
        return (modifierList.findAnnotation(JaxrsRequestAnnotation.PATH.getQualifiedName()) != null);
    }

    public PsiMethodHelper withModule(Module module) {
        this.myModule = module;
        return this;
    }

    @NotNull
    protected Project getProject() {
        myProject = psiMethod.getProject();
        return myProject;
    }

    public String buildParamString() {
        StringBuilder param = new StringBuilder("");
        Map<String, Object> baseTypeParamMap = getBaseTypeParameterMap();

        if (baseTypeParamMap != null && baseTypeParamMap.size() > 0) {
            baseTypeParamMap.forEach((s, o) -> param.append(s).append("=").append(o).append("&"));
        }

        return param.length() > 0 ? param.deleteCharAt(param.length() - 1).toString() : "";
    }

    @NotNull
    public Map<String, Object> getBaseTypeParameterMap() {
        List<Parameter> parameterList = getParameterList();

        Map<String, Object> baseTypeParamMap = new LinkedHashMap();
        for (Parameter parameter : parameterList) {
            if (parameter.isRequestBodyFound()) {
                continue;
            }

            // todo type check
            // 8 PsiPrimitiveType
            // 8 boxed types; String,Date:PsiClassReferenceType == field.getType().getPresentableText()
            String shortTypeName = parameter.getShortTypeName();
            Object defaultValue = PsiClassHelper.getJavaBaseTypeDefaultValue(shortTypeName);
            if (defaultValue != null) {
                baseTypeParamMap.put(parameter.getParamName(), (defaultValue));
                continue;
            }

            PsiClassHelper psiClassHelper = PsiClassHelper.create(psiMethod.getContainingClass());
            PsiClass psiClass = psiClassHelper.findOnePsiClassByClassName(parameter.getParamType(), getProject());

            if (psiClass != null) {
                PsiField[] fields = psiClass.getFields();
                for (PsiField field : fields) {
                    Object fieldDefaultValue = PsiClassHelper.getJavaBaseTypeDefaultValue(field.getType().getPresentableText());
                    if (fieldDefaultValue != null) {
                        baseTypeParamMap.put(field.getName(), fieldDefaultValue);
                    }
                }
            }
        }
        return baseTypeParamMap;
    }

    @Nullable
    public Map<String, Object> getJavaBaseTypeDefaultValue(String paramName, String paramType) {
        Map<String, Object> paramMap = new LinkedHashMap<>();
        Object paramValue = null;
        paramValue = PsiClassHelper.getJavaBaseTypeDefaultValue(paramType);
        if (paramValue != null) {
            paramMap.put(paramType, paramValue);
        }
        return paramMap;
    }

    @NotNull
    public List<Parameter> getParameterList() {
        List<Parameter> parameterList = new ArrayList<>();

        PsiParameterList psiParameterList = psiMethod.getParameterList();
        PsiParameter[] psiParameters = psiParameterList.getParameters();
        for (PsiParameter psiParameter : psiParameters) {
            PsiType paramPsiType = psiParameter.getType();
            String paramType = paramPsiType.getCanonicalText();
            if ("javax.servlet.http.HttpServletRequest".equals(paramType) || "javax.servlet.http.HttpServletResponse".equals(paramType)) {
                continue;
            }
            // @RequestParam
            PsiModifierList modifierList = psiParameter.getModifierList();
            boolean requestBodyFound = modifierList.findAnnotation(SpringRequestParamAnnotations.REQUEST_BODY.getQualifiedName()) != null;
            String paramName = psiParameter.getName();
            String requestName = null;

            PsiAnnotation pathVariableAnno = modifierList.findAnnotation(SpringRequestParamAnnotations.PATH_VARIABLE.getQualifiedName());
            if (pathVariableAnno != null) {
                requestName = getAnnotationValue(pathVariableAnno);
                Parameter parameter = new Parameter(paramType, requestName != null ? requestName : paramName).setRequired(true).requestBodyFound(requestBodyFound);
                parameterList.add(parameter);
            }

            PsiAnnotation requestParamAnno = modifierList.findAnnotation(SpringRequestParamAnnotations.REQUEST_PARAM.getQualifiedName());
            if (requestParamAnno != null) {
                requestName = getAnnotationValue(requestParamAnno);
                Parameter parameter = new Parameter(paramType, requestName != null ? requestName : paramName).setRequired(true).requestBodyFound(requestBodyFound);
                parameterList.add(parameter);
            }

            if (pathVariableAnno == null && requestParamAnno == null) {
                if (!paramType.contains("java.util.") && paramType.contains("<") && paramType.contains(">")) {
                    PsiTypeElement typeElement = psiParameter.getTypeElement();
                    if (typeElement == null) {
                        return parameterList;
                    }
                    // Generics param found.  For example: get(PageParam<VetReq> req)
                    PsiJavaCodeReferenceElement referenceElement = typeElement.getInnermostComponentReferenceElement();
                    if (referenceElement == null) {
                        return parameterList;
                    }

                    String tmpParamType = referenceElement.getCanonicalText();

                    if (tmpParamType != null) {
                        Parameter parameter = new Parameter(tmpParamType, paramName, true).requestBodyFound(requestBodyFound);
                        parameterList.add(parameter);
                    }
                } else {
                    Parameter parameter = new Parameter(paramType, paramName).requestBodyFound(requestBodyFound);
                    parameterList.add(parameter);
                }
            }
        }
        return parameterList;
    }

    public String getAnnotationValue(PsiAnnotation annotation) {
        String paramName = null;
        PsiAnnotationMemberValue attributeValue = annotation.findDeclaredAttributeValue("value");

        if (attributeValue != null && attributeValue instanceof PsiLiteralExpression) {
            paramName = (String) ((PsiLiteralExpression) attributeValue).getValue();
        }
        return paramName;
    }

    public String buildRequestBodyJson(Parameter parameter) {
        Project project = psiMethod.getProject();
        final String className = parameter.getParamType();
        final String paramName = parameter.getParamName();

        return PsiClassHelper.create(Objects.requireNonNull(psiMethod.getContainingClass())).withModule(myModule).convertClassToJSON(className, paramName, project);
    }

    public String buildRequestBodyJson() {
        List<Parameter> parameterList = this.getParameterList();
        for (Parameter parameter : parameterList) {
            if (parameter.isRequestBodyFound()) {
                return buildRequestBodyJson(parameter);
            }
        }
        return null;
    }

    @NotNull
    public String buildServiceUriPath() {
        String ctrlPath = null;
        String methodPath = null;
        PsiClass containingClass = psiMethod.getContainingClass();
        RestSupportedAnnotationHelper annotationHelper;
        if (isSpringRestSupported(containingClass)) {
            ctrlPath = RequestMappingAnnotationHelper.getOneRequestMappingPath(containingClass);
            methodPath = RequestMappingAnnotationHelper.getOneRequestMappingPath(psiMethod);
        } else if (isJaxrsRestSupported(containingClass)) {
            ctrlPath = JaxrsAnnotationHelper.getClassUriPath(containingClass);
            methodPath = JaxrsAnnotationHelper.getMethodUriPath(psiMethod);
        }

        if (ctrlPath == null) {
            return null;
        }

        if (!ctrlPath.startsWith("/")) {
            ctrlPath = "/".concat(ctrlPath);
        }
        if (!ctrlPath.endsWith("/")) {
            ctrlPath = ctrlPath.concat("/");
        }
        if (methodPath.startsWith("/")) {
            methodPath = methodPath.substring(1, methodPath.length());
        }

        return ctrlPath + methodPath;
    }

    @NotNull
    public String buildServiceUriPathWithParams() {
        String serviceUriPath = buildServiceUriPath();
        String params = PsiMethodHelper.create(psiMethod).buildParamString();
        if (!params.isEmpty()) {
            StringBuilder urlBuilder = new StringBuilder(serviceUriPath);
            return urlBuilder.append(serviceUriPath.contains("?") ? "&" : "?").append(params).toString();
        }
        return serviceUriPath;
    }


}
