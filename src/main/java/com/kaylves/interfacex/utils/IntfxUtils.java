package com.kaylves.interfacex.utils;

import com.kaylves.interfacex.annotations.InterfaceXEnum;
import com.kaylves.interfacex.annotations.http.SpringHttpRequestAnnotation;
import com.kaylves.interfacex.bean.RocketMQProducerExportBean;
import com.kaylves.interfacex.bean.ServiceExportBean;
import com.kaylves.interfacex.bean.ModulePropertiesExportBean;
import com.kaylves.interfacex.bean.ServiceExportBeanI;
import com.kaylves.interfacex.common.spring.RequestMappingAnnotationHelper;
import com.kaylves.interfacex.method.RequestPath;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lang.java.JavaFindUsagesProvider;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.util.MoveRenameUsageInfo;
import com.intellij.usageView.UsageInfo;
import com.intellij.util.Query;
import com.kaylves.interfacex.strategy.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.*;

/**
 * @author kaylves
 * @since 1.0
 */
@Slf4j
public class IntfxUtils {

    public static List<ServiceExportBeanI> getServiceExportBeans(Project project, InterfaceXEnum... interfaceXEnums)  {

        List<ServiceStrategy> serviceStrateties = new ArrayList<>();

        for (InterfaceXEnum interfaceXEnum : interfaceXEnums) {
            Class<?>  stratetyClass = interfaceXEnum.getStrategy();
            try {
                serviceStrateties.add((ServiceStrategy) stratetyClass.newInstance());
            }catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        List<ServiceExportBeanI> serviceExportBeans = new ArrayList<>();
        serviceStrateties.forEach(serviceStratety -> serviceExportBeans.addAll(serviceStratety.obtainServiceExportBeans(project)));

        serviceExportBeans.sort((o1, o2) -> {

            if(o1 instanceof RocketMQProducerExportBean && o2 instanceof RocketMQProducerExportBean){
                return ((RocketMQProducerExportBean) o1).getTag().compareTo(((RocketMQProducerExportBean) o2).getTag());

            }

            return 0;
        });

        return serviceExportBeans;
    }

    public static List<ServiceExportBean> getServiceExportBeans(Project project) {

        List<ServiceStrategy> serviceStrategy = new ArrayList<>();

        serviceStrategy.add(new OpenFeignStrategy());
        serviceStrategy.add(new SpringControllerStrategy());
        serviceStrategy.add(new XXLJobServiceStrategy());
        serviceStrategy.add(new MissionStrategy());

        List<ServiceExportBean> serviceExportBeans = new ArrayList<>();

        serviceStrategy.forEach(serviceStratety -> serviceExportBeans.addAll(serviceStratety.obtainServiceExportBeans(project)));

        return serviceExportBeans;
    }

    public static void groupOpenFeignTest(Module module, Project project, GlobalSearchScope globalSearchScope, List<ServiceExportBean> serviceExportBeans) {
        Collection<PsiAnnotation> psiAnnotations;

        psiAnnotations = JavaAnnotationIndex.getInstance().get(SpringHttpRequestAnnotation.FEIGN_CLIENT.getShortName(), project, globalSearchScope);


        for (PsiAnnotation psiAnnotation : psiAnnotations) {
            PsiModifierList psiModifierList = (PsiModifierList) psiAnnotation.getParent();
            PsiElement psiElement = psiModifierList.getParent();
            PsiClass psiClass = (PsiClass) psiElement;

            String serviceName = PsiAnnotationHelper.getAnnotationAttributeValue(psiAnnotation, "value");

            PsiMethod[] psiMethods = psiClass.getMethods();

            List<RequestPath> classRequestPaths = RequestMappingAnnotationHelper.getSpringAnnotationRequestPaths(psiClass);

            for (PsiMethod psiMethod : psiMethods) {

                if (!"zmgoRefund".equals(psiMethod.getName())) {
                    continue;
                }

                String name = IdeaPluginUtils.obtainDocAsString(psiMethod);

                FindUsagesProvider findUsagesProvider = new JavaFindUsagesProvider();

                Boolean result = findUsagesProvider.canFindUsagesFor(psiMethod);

                //获取方法引用类
                Query<PsiReference> psiReferences = ReferencesSearch.search(psiMethod, globalSearchScope);

                List<UsageInfo> usages = new ArrayList<>();
                for (PsiReference ref : psiReferences.findAll()) {
                    usages.add(new MoveRenameUsageInfo(ref.getElement(), ref, ref.getRangeInElement().getStartOffset(),
                            ref.getRangeInElement().getEndOffset(), psiClass,
                            ref.resolve() == null && !(ref instanceof PsiPolyVariantReference && ((PsiPolyVariantReference) ref).multiResolve(true).length > 0)));
                }

                usages.forEach(usageInfo -> {
                    log.info("toString:{}", usageInfo.toString());
                    PsiFile psiFile = usageInfo.getFile();
                    log.info("psiFile:{}", psiFile);
                    log.info("psiFile name:{}", PsiUtil.getName(psiFile));
                    log.info("psiFile:getClass{}", psiFile.getTextOffset());
                    log.info("getFile:getStartOffsetInParent{}", psiFile.getStartOffsetInParent());
                    log.info("psiFile:getClass{}", psiFile.getClass());
                    //getText是类所有内容
                    //log.info("psiFile:getText{}",usageInfo.getFile().getText());
                    log.info("getTooltipText:{}", usageInfo.getTooltipText());
                    log.info("getSegment:{}", usageInfo.getSegment());
                });
            }
        }
    }

    public static String getRequestPath(RequestPath classRequestPath, RequestPath methodRequestPath) {
        String classUriPath = classRequestPath.getPath();

        if (!classUriPath.startsWith("/")) {
            classUriPath = "/".concat(classUriPath);
        }

        if (!classUriPath.endsWith("/")) {
            classUriPath = classUriPath.concat("/");
        }
        String methodPath = methodRequestPath.getPath();

        if (methodPath.startsWith("/")) {
            methodPath = methodPath.substring(1);
        }

        return classUriPath + methodPath;
    }

    public static String getRequestPath(String classUriPath, RequestPath methodRequestPath) {

        if (!classUriPath.startsWith("/")) {
            classUriPath = "/".concat(classUriPath);
        }

        if (!classUriPath.endsWith("/")) {
            classUriPath = classUriPath.concat("/");
        }
        String methodPath = methodRequestPath.getPath();

        if (methodPath.startsWith("/")) {
            methodPath = methodPath.substring(1);
        }

        return classUriPath + methodPath;
    }

    /**
     * 获取所有模块的message_zh.properties
     * @param project   idea工程
     * @return  返回List
     */
    public static List<ModulePropertiesExportBean> getModulePropertiesExportBeans(Project project) {
        Module[] modules = ModuleManager.getInstance(project).getModules();

        List<ModulePropertiesExportBean> list = new ArrayList<>();
        Arrays.stream(modules).forEach(module -> {

            try {
                Properties properties = new Properties();

                PsiFile psiFile = findPsiFileInModule("message_zh.properties", module);
                if(psiFile==null){
                    return;
                }

                properties.load(psiFile.getVirtualFile().getInputStream());

                properties.keySet().forEach(object -> {
                    ModulePropertiesExportBean modulePropertiesExportBean = new ModulePropertiesExportBean();
                    modulePropertiesExportBean.setModelName(module.getName());
                    modulePropertiesExportBean.setKey(object.toString());
                    modulePropertiesExportBean.setValue(properties.getProperty(object.toString()));
                    list.add(modulePropertiesExportBean);
                });

            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        return list;
    }


    private static PsiFile findPsiFileInModule(String fileName, Module module) {
        PsiFile psiFile = null;
        PsiFile[] applicationProperties = FilenameIndex.getFilesByName(
                module.getProject(),
                fileName,
                GlobalSearchScope.moduleScope(module)
        );

        if (applicationProperties.length > 0) {
            psiFile = applicationProperties[0];
        }

        return psiFile;
    }
}
