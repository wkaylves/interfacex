package com.kaylves.interfacex.action.navigation;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.psi.PsiMethod;
import com.kaylves.interfacex.finder.ControllerTargetFinder;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Slf4j
public class ControllerNavigationAction extends AnAction {

    private final PsiMethod sourceMethod;
    private List<PsiMethod> targetMethods;

    public ControllerNavigationAction(@NotNull PsiMethod sourceMethod, @NotNull List<PsiMethod> targetMethods) {
        this.sourceMethod = sourceMethod;
        this.targetMethods = targetMethods;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        if (targetMethods == null || targetMethods.isEmpty()) {
            targetMethods = ControllerTargetFinder.findTargetFeignClients(sourceMethod);
        }

        if (targetMethods.size() == 1) {
            navigateToTarget(targetMethods.get(0));
        } else if (targetMethods.size() > 1) {
            log.info("Multiple targets found: {}, showing selection UI not yet implemented", targetMethods.size());
        }
    }

    private void navigateToTarget(PsiMethod targetMethod) {
        if (targetMethod.canNavigate()) {
            targetMethod.navigate(true);
        }
    }
}