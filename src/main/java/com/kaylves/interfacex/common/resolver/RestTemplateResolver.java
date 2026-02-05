package com.kaylves.interfacex.common.resolver;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.kaylves.interfacex.method.HttpMethod;
import com.kaylves.interfacex.ui.navigator.ServiceItem;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
public class RestTemplateResolver extends BaseServiceResolver {

    public RestTemplateResolver(Module module) {
        this.module = module;
    }

    @Override
    public List<ServiceItem> getRestServiceItemList(Project project, GlobalSearchScope globalSearchScope) {
        return findProducerCalls(project, this.module);
    }

    private List<ServiceItem> findProducerCalls(Project project, Module module) {
        String[] methodsByNames = {"postForObject","postForEntity","getForObject","getForEntity"};

        String[] targetClassNames = {
                "org.springframework.web.client.RestTemplate"
        };

        GlobalSearchScope scope = GlobalSearchScope.everythingScope(project);

        GlobalSearchScope moduleScope = GlobalSearchScope.moduleScope(module);

        List<ServiceItem> results = new ArrayList<>();

        for (String methodsByName : methodsByNames) {

            for (String className : targetClassNames) {
                PsiClass psiClass = JavaPsiFacade.getInstance(project).findClass(className, scope);
                if (psiClass == null) continue;

                for (PsiMethod method : psiClass.findMethodsByName(methodsByName, false)) {
                    Collection<PsiReference> references = ReferencesSearch.search(method, moduleScope).findAll();
                    for (PsiReference ref : references) {
                        PsiElement element = ref.getElement();

                        if (element instanceof PsiReferenceExpression) {
                            PsiElement parent = element.getParent();
                            if (parent instanceof PsiMethodCallExpression callExpression) {
                                addCall(results, callExpression, methodsByName);
                            }
                        }
                    }
                }
            }
        }

        return results;
    }

    private void addCall(List<ServiceItem> results, PsiMethodCallExpression callExpr, String callType) {

        PsiElement parent = PsiTreeUtil.getParentOfType(callExpr, PsiMethod.class);

        if (!(parent instanceof PsiMethod method)) {
            return;
        }

        PsiElement psiMethodParent = method.getParent();

        if(psiMethodParent instanceof PsiClass psiClass){
            //过滤测试包类文件
            if(PsiAnnotationHelper.isTestPackage(psiClass)){
                return;
            }
        }

        String requestMethod = HttpMethod.POST.name();

        results.add(new ServiceItem(method, null, requestMethod, method.getName(), false));

    }

    @Override
    public String getServiceItem() {
        return "RestTemplate";
    }
}
