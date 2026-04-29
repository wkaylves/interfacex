package com.kaylves.interfacex.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiMethod;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.finder.RocketMQTargetFinder;
import com.kaylves.interfacex.module.rocketmq.RocketMQDeliverAnnotation;
import com.kaylves.interfacex.module.rocketmq.RocketMQProducerAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * RocketMQ Producer行标记 - 跳转到对应的Listener
 */
@Slf4j
public class RocketMQProducerLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiIdentifier)) {
            return null;
        }
        
        PsiElement parent = element.getParent();
        if (!(parent instanceof PsiMethod)) {
            return null;
        }

        PsiMethod method = (PsiMethod) parent;
        
        if (!isRocketMQProducerMethod(method)) {
            return null;
        }

        List<PsiMethod> targets = RocketMQTargetFinder.findTargetListeners(method);
        if (targets.isEmpty()) {
            return null;
        }

        return createLineMarker(method, targets, "跳转到 RocketMQ Listener");
    }

    private boolean isRocketMQProducerMethod(PsiMethod method) {
        // 检查@Deliver注解
        PsiAnnotation deliverAnnotation = method.getAnnotation(RocketMQDeliverAnnotation.PATH.getQualifiedName());
        if (deliverAnnotation != null) {
            return true;
        }
        
        // 检查自定义Producer注解
        for (RocketMQProducerAnnotation producerAnnotation : RocketMQProducerAnnotation.getPathArray()) {
            PsiAnnotation annotation = method.getAnnotation(producerAnnotation.getQualifiedName());
            if (annotation != null) {
                return true;
            }
        }
        
        return false;
    }

    private LineMarkerInfo<PsiElement> createLineMarker(PsiMethod source, List<PsiMethod> targets, String tooltip) {
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(ToolkitIcons.METHOD.PRODUCE)
            .setTooltipText(tooltip)
            .setTargets(targets)
            .setAlignment(GutterIconRenderer.Alignment.RIGHT);

        return builder.createLineMarkerInfo(source);
    }
}
