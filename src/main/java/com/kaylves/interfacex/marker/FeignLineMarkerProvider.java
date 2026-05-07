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
import com.kaylves.interfacex.finder.OpenFeignTargetFinder;
import com.kaylves.interfacex.module.http.springmvc.SpringHttpRequestAnnotation;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Slf4j
public class FeignLineMarkerProvider implements LineMarkerProvider {

    private static final String FEIGN_CLIENT_QUALIFIED_NAME = SpringHttpRequestAnnotation.FEIGN_CLIENT.getQualifiedName();

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return null;
        }

        PsiMethod method = (PsiMethod) element;
        if (!isFeignClientMethod(method)) {
            return null;
        }

        List<PsiMethod> targets = OpenFeignTargetFinder.findTargetControllers(method);
        if (targets.isEmpty()) {
            log.debug("No target controllers found for Feign method: {}", method.getName());
            return null;
        }

        log.debug("Found {} target controllers for Feign method: {}", targets.size(), method.getName());
        return createLineMarker(method, targets.get(0));
    }

    private boolean isFeignClientMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        PsiAnnotation feignAnnotation = containingClass.getAnnotation(FEIGN_CLIENT_QUALIFIED_NAME);
        if (feignAnnotation == null) {
            return false;
        }
        log.debug("Feign client detected: {}", containingClass.getQualifiedName());
        return true;
    }

    private LineMarkerInfo<PsiElement> createLineMarker(PsiMethod source, PsiMethod target) {
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.Method)
            .setTooltipText("跳转到 Controller")
            .setTarget(target)
            .setAlignment(GutterIconRenderer.Alignment.RIGHT);

        return builder.createLineMarkerInfo(source);
    }
}