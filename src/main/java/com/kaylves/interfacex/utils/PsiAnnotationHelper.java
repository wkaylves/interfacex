package com.kaylves.interfacex.utils;

import com.intellij.psi.*;
import com.intellij.psi.impl.compiled.ClsFieldImpl;
import com.intellij.psi.impl.source.PsiFieldImpl;
import com.intellij.psi.impl.source.tree.java.PsiBinaryExpressionImpl;
import com.intellij.psi.impl.source.tree.java.PsiPolyadicExpressionImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author kaylves
 * @since 1.5.0
 */
@Slf4j
public class PsiAnnotationHelper {

    /**
     * 私有构建
     */
    private PsiAnnotationHelper() {
    }

    @NotNull
    public static List<String> getAnnotationAttributeValues(PsiAnnotation annotation, String attr) {
        List<String> values = new ArrayList<>();

        if (annotation == null) {
            return values;
        }

        PsiAnnotationMemberValue annotationMemberValue = annotation.findDeclaredAttributeValue(attr);

        if (annotationMemberValue == null) {
            return values;
        }

        if (annotationMemberValue instanceof PsiExpression psiExpression) {
            values.add(getValue(psiExpression).toString());
        } else if (annotationMemberValue instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) annotationMemberValue).getInitializers();
            for (PsiAnnotationMemberValue initializer : initializers) {
                values.add(initializer.getText().replaceAll("\\\"", ""));
            }
        } else {
            values.add(annotationMemberValue.getText());
        }

        return values;
    }

    /**
     * 获取表达式值
     *
     * @param psiExpression 表达式
     * @return 返回表达式值
     */
    public static Object getValue(PsiExpression psiExpression) {

        if (psiExpression instanceof PsiLiteralExpression psiLiteralExpression) {
            return psiLiteralExpression.getValue();
        }

        //引用表达式计算
        if (psiExpression instanceof PsiReferenceExpression expression) {
            PsiElement resolvedElement = expression.resolve();
            // 如果解析后的元素是一个字面量表达式，可以将其转换为字符串
            if (resolvedElement instanceof PsiLiteralExpression psiLiteralExpression) {
                return psiLiteralExpression.getValue();
            } else if (resolvedElement instanceof PsiFieldImpl psiField) {
                // 检查字段是否是静态常量
                if (psiField.hasInitializer()) {
                    // 获取字段的初始值
                    PsiElement initializer = psiField.getInitializer();

                    // 如果初始值是一个字面量表达式，可以将其转换为字符串
                    if (initializer instanceof PsiLiteralExpression psiLiteralExpression) {
                        return psiLiteralExpression.getValue();
                    }
                    //引用中包含二元运算
                    else if (initializer instanceof PsiBinaryExpressionImpl psiBinaryExpression) {
                        return getValue(psiBinaryExpression);
                    }
                }
            } else if (resolvedElement instanceof ClsFieldImpl psiField) {
                // 检查字段是否是静态常量
                if (psiField.hasInitializer()) {
                    // 获取字段的初始值
                    PsiElement initializer = psiField.getInitializer();

                    // 如果初始值是一个字面量表达式，可以将其转换为字符串
                    if (initializer instanceof PsiLiteralExpression psiLiteralExpression) {
                        return psiLiteralExpression.getValue();
                    }
                    //引用中包含二元运算
                    else if (initializer instanceof PsiBinaryExpressionImpl psiBinaryExpression) {
                        return getValue(psiBinaryExpression);
                    }
                }
            } else if (resolvedElement instanceof PsiReferenceExpression psiReferenceExpression) {
                return getValue(psiReferenceExpression);
            } else {
                //默认实现
                return expression.getText();
            }
        }

        if (psiExpression instanceof PsiBinaryExpressionImpl psiBinaryExpression) {
            return getValue(psiBinaryExpression.getLOperand()).toString() + getValue(psiBinaryExpression.getROperand()).toString();
        }

        //多操作表达式
        if (psiExpression instanceof PsiPolyadicExpressionImpl psiPolyadicExpression) {

            PsiExpression[] psiExpressions = psiPolyadicExpression.getOperands();

            StringBuffer sb = new StringBuffer();
            Arrays.stream(psiExpressions).forEach(expression -> {
                sb.append(getValue(expression));
            });

            return sb.toString();
        }

        return "";
    }

    /**
     * 获取表达式值
     * @param annotation psiAnnotation
     * @param attr  attr名称
     * @return 返回值
     */
    public static String getAnnotationAttributeValue(PsiAnnotation annotation, String attr) {

        List<String> values = getAnnotationAttributeValues(annotation, attr);

        if (!values.isEmpty()) {
            return values.get(0);
        }

        return "";
    }

    /**
     * 是否是测试包
     * @param psiClass psiClass
     * @return isTestPackage
     */
    public static boolean isTestPackage(PsiClass psiClass){
        return psiClass.getContainingFile().getVirtualFile().getPath().contains("/src/test/java");
    }

    public static String getOneQueue(PsiAnnotation psiAnnotation) {
        AtomicReference<String> path = new AtomicReference<>("");

        final String queue = StringUtils.trimToEmpty(PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "queues"));

        if (StringUtils.isNotBlank(queue)) {
            path.set(queue);
        } else {

            PsiAnnotationMemberValue annotationMemberValue = psiAnnotation.findDeclaredAttributeValue("queuesToDeclare");

            if (annotationMemberValue instanceof PsiArrayInitializerMemberValue psiArrayInitializerMemberValue) {
                PsiAnnotationMemberValue[] memberValues = psiArrayInitializerMemberValue.getInitializers();

                Optional<PsiAnnotationMemberValue> optionalPsiAnnotationMemberValue = Arrays.stream(memberValues).findFirst();

                optionalPsiAnnotationMemberValue.ifPresent(psiAnnotationMemberValue -> {

                    if (psiAnnotationMemberValue instanceof PsiAnnotation annotation) {
                        String queue2 = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "value");
                        path.set(queue2);
                    }
                });
            }
        }

        return path.get();
    }
}
