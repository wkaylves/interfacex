package com.kaylves.interfacex.marker;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.kaylves.interfacex.finder.ControllerTargetFinder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Slf4j
public class ControllerLineMarkerProvider implements LineMarkerProvider {

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (!(element instanceof PsiMethod)) {
            return null;
        }

        PsiMethod method = (PsiMethod) element;
        if (!isControllerMethod(method)) {
            return null;
        }

        List<PsiMethod> targets = ControllerTargetFinder.findTargetFeignClients(method);
        if (targets.isEmpty()) {
            return null;
        }

        return createLineMarker(method, targets.get(0));
    }

    private boolean isControllerMethod(PsiMethod method) {
        PsiClass containingClass = method.getContainingClass();
        if (containingClass == null) {
            return false;
        }
        String qualifiedName = containingClass.getQualifiedName();
        if (qualifiedName == null) {
            return false;
        }
        return hasControllerAnnotation(containingClass);
    }

    private boolean hasControllerAnnotation(PsiClass psiClass) {
        com.intellij.psi.PsiAnnotation[] annotations = psiClass.getModifierList().getAnnotations();
        if (annotations == null) {
            return false;
        }
        for (com.intellij.psi.PsiAnnotation annotation : annotations) {
            String name = annotation.getQualifiedName();
            if (name != null) {
                if (name.endsWith("RestController") ||
                    name.endsWith(".Controller") ||
                    name.endsWith("Controller") && !name.endsWith("FeignClient")) {
                    return true;
                }
            }
        }
        return false;
    }

    private LineMarkerInfo<PsiElement> createLineMarker(PsiMethod source, PsiMethod target) {
        NavigationGutterIconBuilder<PsiElement> builder = NavigationGutterIconBuilder.create(AllIcons.Nodes.Method)
            .setTooltipText("跳转到 FeignClient")
            .setTarget(target)
            .setAlignment(GutterIconRenderer.Alignment.RIGHT);

        return builder.createLineMarkerInfo(source);
    }
}