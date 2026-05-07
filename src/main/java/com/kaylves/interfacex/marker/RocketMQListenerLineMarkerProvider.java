package com.kaylves.interfacex.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.finder.RocketMQTargetFinder;
import com.kaylves.interfacex.module.rocketmq.RocketMQAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * RocketMQ Listener行标记 - 跳转到对应的Producer
 */
@Slf4j
public class RocketMQListenerLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return null;
        }

        PsiMethod method = (PsiMethod) element;
        if (!isRocketMQListenerMethod(method)) {
            return null;
        }

        List<PsiMethod> targets = RocketMQTargetFinder.findTargetProducers(method);
        if (targets.isEmpty()) {
            return null;
        }

        return createLineMarker(method, targets, "跳转到 RocketMQ Producer");
    }

    private boolean isRocketMQListenerMethod(PsiMethod method) {
        // 检查方法名是否为consume
        if (!"consume".equals(method.getName())) {
            return false;
        }

        // 检查所在类是否有RocketMQ Listener注解
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }

        // 支持所有RocketMQAnnotation类型的注解
        for (RocketMQAnnotation annotationType : RocketMQAnnotation.values()) {
            PsiAnnotation annotation = containingClass.getAnnotation(annotationType.getQualifiedName());
            if (annotation != null) {
                return true;
            }
        }
        
        return false;
    }

    private LineMarkerInfo<PsiElement> createLineMarker(PsiMethod source, List<PsiMethod> targets, String tooltip) {
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ToolkitIcons.METHOD.CONSUME)
            .setTooltipText(tooltip)
            .setTargets(targets)
            .setAlignment(GutterIconRenderer.Alignment.RIGHT);

        return builder.createLineMarkerInfo(source);
    }
}
