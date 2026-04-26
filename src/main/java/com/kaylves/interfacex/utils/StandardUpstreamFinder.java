package com.kaylves.interfacex.utils;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Processor;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * 上游入口查找器 - 标准 ReferencesSearch 实现
 * 依赖：com.intellij.modules.lang
 */
public class StandardUpstreamFinder {

    private static final int MAX_DEPTH = 20;
    private static final int MAX_BRANCHES_PER_NODE = 50;

    public List<EntranceResult> findUpstreamEntrances(@NotNull PsiMethod startMethod, @Nullable ProgressIndicator indicator) {
        List<EntranceResult> results = new ArrayList<>();
        Set<String> visitedInPath = new HashSet<>();
        Deque<String> pathStack = new ArrayDeque<>();

        String startSig = getMethodSignature(startMethod);
        pathStack.push(startSig);
        visitedInPath.add(startSig);

        dfsSearch(startMethod, visitedInPath, pathStack, results, 0, indicator);
        return results;
    }

    private void dfsSearch(@NotNull PsiMethod currentMethod,
                           @NotNull Set<String> visitedInPath,
                           @NotNull Deque<String> pathStack,
                           @NotNull List<EntranceResult> results,
                           int depth,
                           @Nullable ProgressIndicator indicator) {

        if (indicator != null && indicator.isCanceled()) return;
        if (depth > MAX_DEPTH) return;

        // 1. 入口检查
        EntranceInfo entryInfo = checkEntranceType(currentMethod);
        if (entryInfo != null) {
            List<String> chain = new ArrayList<>(pathStack);
            Collections.reverse(chain);
            results.add(new EntranceResult(currentMethod, entryInfo.type, entryInfo.identifier, chain));
            return;
        }

        // 2. 【核心】使用 ReferencesSearch 查找调用者
        List<PsiMethod> callers = findCallersUsingReferencesSearch(currentMethod, indicator);

        // 限流，防止爆炸
        if (callers.size() > MAX_BRANCHES_PER_NODE) {
            callers = callers.subList(0, MAX_BRANCHES_PER_NODE);
        }

        for (PsiMethod caller : callers) {
            String callerSig = getMethodSignature(caller);
            if (visitedInPath.contains(callerSig)) continue;

            pathStack.push(callerSig);
            visitedInPath.add(callerSig);

            dfsSearch(caller, visitedInPath, pathStack, results, depth + 1, indicator);

            pathStack.pop();
            visitedInPath.remove(callerSig);

            if (indicator != null && indicator.isCanceled()) return;
        }
    }

    /**
     * 修正版：使用正确的 Query<PsiReference> 处理逻辑
     */
    @NotNull
    private List<PsiMethod> findCallersUsingReferencesSearch(@NotNull PsiMethod targetMethod, @Nullable ProgressIndicator indicator) {
        List<PsiMethod> callers = new ArrayList<>();
        Project project = targetMethod.getProject();

        // 1. 定义搜索范围：整个项目
        GlobalSearchScope scope = GlobalSearchScope.projectScope(project);

        // 2. 执行搜索
        // 返回值是 Query<PsiReference>，不是 PsiReferencesCollector
        Query<PsiReference> query = ReferencesSearch.search(targetMethod, scope, true);

        // 3. 遍历结果 (方式一：使用 forEach + Processor 回调，支持取消检查)
        query.forEach(new Processor<PsiReference>() {
            @Override
            public boolean process(PsiReference reference) {
                // 检查是否被用户取消
                if (indicator != null && indicator.isCanceled()) {
                    return false; // 返回 false 停止遍历
                }

                PsiElement element = reference.getElement();

                // 向上查找包裹该引用的方法
                PsiMethod callerMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);

                if (callerMethod != null) {
                    // 去重：避免同一个方法内多次调用导致重复添加
                    if (!callers.contains(callerMethod)) {
                        callers.add(callerMethod);
                    }
                }

                return true; // 返回 true 继续遍历下一个
            }
        });

    /*
    // 方式二：如果你不需要在遍历中频繁检查 cancel，也可以用增强 for 循环 (更简洁)
    for (PsiReference reference : query) {
        if (indicator != null && indicator.isCanceled()) break;

        PsiElement element = reference.getElement();
        PsiMethod callerMethod = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (callerMethod != null && !callers.contains(callerMethod)) {
            callers.add(callerMethod);
        }
    }
    */

        return callers;
    }

    // --- 辅助方法 (与之前逻辑一致) ---

    private boolean isSameMethod(@NotNull PsiMethod m1, @NotNull PsiMethod m2) {
        if (!m1.getName().equals(m2.getName())) return false;
        PsiClass c1 = m1.getContainingClass();
        PsiClass c2 = m2.getContainingClass();
        if (c1 == null || c2 == null) return false;
        String q1 = c1.getQualifiedName();
        String q2 = c2.getQualifiedName();
        return q1 != null && q1.equals(q2);
    }

    @Nullable
    private EntranceInfo checkEntranceType(@NotNull PsiMethod method) {
        PsiAnnotation[] annotations = method.getAnnotations();
        if (annotations != null) {
            for (PsiAnnotation ann : annotations) {
                String qName = ann.getQualifiedName();
                if (qName == null) continue;

                if (qName.contains("RequestMapping") || qName.contains("GetMapping") || qName.contains("PostMapping"))
                    return new EntranceInfo(EntranceType.HTTP, extractUrl(ann));
                if (qName.contains("KafkaListener"))
                    return new EntranceInfo(EntranceType.MQ_KAFKA, "Topic");
                if (qName.contains("XxlJob"))
                    return new EntranceInfo(EntranceType.XXL_JOB, "Job");
                if (qName.contains("EventListener"))
                    return new EntranceInfo(EntranceType.SPRING_EVENT, extractEventTypeFromEventListener(method, ann));
            }
        }

        // 检查 ApplicationListener 接口
        PsiClass containingClass = method.getContainingClass();
        if (containingClass != null) {
            String eventType = extractEventTypeFromApplicationListener(containingClass);
            if (eventType != null) {
                return new EntranceInfo(EntranceType.SPRING_EVENT, eventType);
            }
        }
        return null;
    }

    @Nullable
    private String extractEventTypeFromEventListener(@NotNull PsiMethod method, @NotNull PsiAnnotation annotation) {
        String classesAttr = extractAttr(annotation, "classes");
        if (classesAttr != null) return classesAttr;
        PsiParameter[] params = method.getParameterList().getParameters();
        if (params.length > 0) {
            return params[0].getType().getPresentableText();
        }
        return null;
    }

    @Nullable
    private String extractEventTypeFromApplicationListener(@NotNull PsiClass containingClass) {
        for (PsiClassType type : containingClass.getImplementsListTypes()) {
            String eventTypeName = extractGenericFromSpringListener(type);
            if (eventTypeName != null) return eventTypeName;
        }
        for (PsiClassType type : containingClass.getExtendsListTypes()) {
            String eventTypeName = extractGenericFromSpringListener(type);
            if (eventTypeName != null) return eventTypeName;
        }
        return null;
    }

    @Nullable
    private String extractGenericFromSpringListener(@NotNull PsiClassType type) {
        String canonicalText = type.getCanonicalText();
        if (canonicalText.startsWith("org.springframework.context.ApplicationListener")) {
            PsiType[] parameters = type.getParameters();
            if (parameters.length > 0) {
                return parameters[0].getPresentableText();
            }
        }
        return null;
    }

    private String extractUrl(PsiAnnotation ann) {
        PsiAnnotationMemberValue val = ann.findDeclaredAttributeValue("value");
        if (val == null) val = ann.findDeclaredAttributeValue("path");
        if (val instanceof PsiLiteralExpression) {
            Object v = ((PsiLiteralExpression) val).getValue();
            return v != null ? v.toString() : "/unknown";
        }
        return "/unknown";
    }

    @Nullable
    private String extractAttr(@NotNull PsiAnnotation annotation, @NotNull String attrName) {
        PsiAnnotationMemberValue value = annotation.findDeclaredAttributeValue(attrName);
        if (value instanceof PsiArrayInitializerMemberValue) {
            PsiAnnotationMemberValue[] initializers = ((PsiArrayInitializerMemberValue) value).getInitializers();
            if (initializers.length > 0 && initializers[0] instanceof PsiLiteralExpression) {
                Object val = ((PsiLiteralExpression) initializers[0]).getValue();
                return val != null ? val.toString() : null;
            }
        }
        if (value instanceof PsiLiteralExpression) {
            Object val = ((PsiLiteralExpression) value).getValue();
            return val != null ? val.toString() : null;
        }
        return null;
    }

    @NotNull
    private String getMethodSignature(@NotNull PsiMethod method) {
        PsiClass psiClass = method.getContainingClass();
        String className = psiClass != null ? psiClass.getQualifiedName() : "Unknown";
        return className + "#" + method.getName();
    }

    // --- 内部类定义 ---
    public enum EntranceType {
        HTTP("HTTP 接口 (UI/Button)"), MQ_KAFKA("Kafka"), XXL_JOB("XXL-JOB"), SPRING_EVENT("Spring Event");
        private final String desc;
        EntranceType(String d) { desc = d; }
        public String getDesc() { return desc; }
    }

    public static class EntranceResult {
        public final PsiMethod method;
        public final EntranceType type;
        public final String identifier;
        public final List<String> callChain;
        public EntranceResult(PsiMethod m, EntranceType t, String i, List<String> c) {
            method=m; type=t; identifier=i; callChain=c;
        }
        @Override
        public String toString() {
            return type.getDesc() + ": " + identifier;
        }
    }

    private static class EntranceInfo {
        EntranceType type; String identifier;
        EntranceInfo(EntranceType t, String i) { type=t; identifier=i; }
    }
}