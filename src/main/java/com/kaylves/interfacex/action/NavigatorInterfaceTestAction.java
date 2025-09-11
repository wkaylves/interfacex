package com.kaylves.interfacex.action;

import com.kaylves.interfacex.utils.IdeaPluginUtils;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.util.Query;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class NavigatorInterfaceTestAction extends AnAction {

    @Nullable
    public static Project getProject(DataContext context) {
        return CommonDataKeys.PROJECT.getData(context);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = getProject(e.getDataContext());
        assert project != null;

        log.info(">>>>>>>>>>>>>>test Action>>>>>>>");

        PsiClass psiClass = IdeaPluginUtils.findPsiClass(project, "org.springframework.web.client.RestTemplate");

        String[] ignoreMethods = {"getMessageConverters", "setErrorHandler"};

        List<PsiMethod> psiMethodList = Arrays.stream(psiClass.getMethods()).filter(psiMethod -> Arrays.stream(ignoreMethods).filter(s -> !psiMethod.getName().equals(s)).count() > 0).toList();

        psiMethodList.forEach(psiMethod -> printMethodDenpency(psiMethod, project, psiClass));

    }

    private static void printMethodDenpency(PsiMethod psiMethod, Project project, PsiClass psiClass) {

        Module[] modules = ModuleManager.getInstance(project).getModules();

        Arrays.stream(modules).forEach(module -> {
            GlobalSearchScope globalSearchScope = GlobalSearchScope.moduleScope(module);

            Query<PsiReference> psiReferenceQuery = ReferencesSearch.search(psiMethod, globalSearchScope);

        });
    }
}
