package com.kaylves.interfacex.finder;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.kaylves.interfacex.module.rocketmq.RocketMQAnnotation;
import com.kaylves.interfacex.module.rocketmq.RocketMQDeliverAnnotation;
import com.kaylves.interfacex.module.rocketmq.RocketMQProducerAnnotation;
import com.kaylves.interfacex.utils.PsiAnnotationHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * RocketMQ Producer和Listener联动查找器
 * 通过tag匹配Producer和Listener
 */
@Slf4j
public class RocketMQTargetFinder {

    private static final String[] DELIVER_TAG_ATTRIBUTES = {"tags", "tag"};
    private static final String[] CUSTOM_NESTED_ATTRIBUTES = {"rocketmqAttrbute", "rocketmqAttribute"};
    private static final String CONSUME_METHOD_NAME = "consume";
    private static final String WILDCARD_TAG = "*";

    /**
     * 查找Producer对应的Listener
     * @param producerMethod Producer方法
     * @return 匹配的Listener方法列表
     */
    public static List<PsiMethod> findTargetListeners(PsiMethod producerMethod) {
        List<PsiMethod> result = new ArrayList<>();

        // 获取Producer的tag(支持多种注解方式)
        String producerTag = getProducerTag(producerMethod);
        log.info("[RocketMQ Finder] Producer method: {}, tag: '{}'", producerMethod.getName(), producerTag);
        
        if (StringUtils.isBlank(producerTag)) {
            log.warn("[RocketMQ Finder] Producer method has no tag: {}", producerMethod.getName());
            return result;
        }

        Project project = producerMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);

        // 查找所有RocketMQ Listener注解(支持多种类型)
        List<PsiAnnotation> allListenerAnnotations = new ArrayList<>();
        for (RocketMQAnnotation listenerAnnotationType : RocketMQAnnotation.values()) {
            Collection<PsiAnnotation> annotations = JavaAnnotationIndex.getInstance()
                .get(listenerAnnotationType.getShortName(), project, searchScope);
            log.info("[RocketMQ Finder] Found {} @{} annotations", annotations.size(), listenerAnnotationType.getShortName());
            allListenerAnnotations.addAll(annotations);
        }
        
        log.info("[RocketMQ Finder] Total listener annotations: {}", allListenerAnnotations.size());
        log.info("[RocketMQ Finder] allListenerAnnotations.isEmpty(): {}", allListenerAnnotations.isEmpty());

        int processedCount = 0;
        for (PsiAnnotation annotation : allListenerAnnotations) {
            processedCount++;
            log.info("[RocketMQ Finder] Processing listener {}/{}...", processedCount, allListenerAnnotations.size());
            PsiClass listenerClass = getClassFromAnnotation(annotation);
            if (listenerClass == null) {
                log.info("[RocketMQ Finder] listenerClass is null, skip");
                continue;
            }

            if (PsiAnnotationHelper.isTestPackage(listenerClass)) {
                log.info("[RocketMQ Finder] Is test package, skip: {}", listenerClass.getName());
                continue;
            }

            // 获取Listener的tag(selectorExpression)
            String listenerTag = getListenerTag(annotation);
            log.info("[RocketMQ Finder] Listener class: {}, tag: '{}'", listenerClass.getName(), listenerTag);
            
            // 匹配tag
            if (tagMatches(producerTag, listenerTag)) {
                log.info("[RocketMQ Finder] Tag matched! Finding consume method...");
                // 找到consume方法
                PsiMethod consumeMethod = findConsumeMethod(listenerClass);
                if (consumeMethod != null) {
                    log.info("[RocketMQ Finder] Found consume method: {}", consumeMethod.getName());
                    result.add(consumeMethod);
                } else {
                    log.warn("[RocketMQ Finder] No consume method found in class: {}", listenerClass.getName());
                }
            }
        }

        log.info("[RocketMQ Finder] Total matched listeners: {}", result.size());
        return result;
    }

    /**
     * 查找Listener对应的Producer
     * @param listenerMethod Listener的consume方法
     * @return 匹配的Producer方法列表
     */
    public static List<PsiMethod> findTargetProducers(PsiMethod listenerMethod) {
        PsiClass listenerClass = listenerMethod.getContainingClass();
        if (listenerClass == null) {
            return new ArrayList<>();
        }

        PsiAnnotation listenerAnnotation = findListenerAnnotation(listenerClass);
        if (listenerAnnotation == null) {
            return new ArrayList<>();
        }

        String listenerTag = getListenerTag(listenerAnnotation);
        if (StringUtils.isBlank(listenerTag)) {
            return new ArrayList<>();
        }

        Project project = listenerMethod.getProject();
        GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
        List<PsiMethod> allProducers = findAllProducers(project, searchScope);

        List<PsiMethod> result = new ArrayList<>();
        for (PsiMethod producerMethod : allProducers) {
            String producerTag = getProducerTag(producerMethod);
            if (tagMatches(producerTag, listenerTag)) {
                result.add(producerMethod);
            }
        }
        return result;
    }

    private static PsiAnnotation findListenerAnnotation(PsiClass listenerClass) {
        for (RocketMQAnnotation annotationType : RocketMQAnnotation.values()) {
            PsiAnnotation annotation = listenerClass.getAnnotation(annotationType.getQualifiedName());
            if (annotation != null) {
                return annotation;
            }
        }
        return null;
    }

    private static List<PsiMethod> findAllProducers(Project project, GlobalSearchScope searchScope) {
        List<PsiMethod> allProducers = new ArrayList<>();
        
        // 查找@Deliver注解
        Collection<PsiAnnotation> deliverAnnotations = JavaAnnotationIndex.getInstance()
            .get(RocketMQDeliverAnnotation.PATH.getShortName(), project, searchScope);
        for (PsiAnnotation annotation : deliverAnnotations) {
            PsiMethod producerMethod = getMethodFromAnnotation(annotation);
            if (isValidProducer(producerMethod)) {
                allProducers.add(producerMethod);
            }
        }
        
        // 查找自定义Producer注解
        for (RocketMQProducerAnnotation producerAnnotation : RocketMQProducerAnnotation.getPathArray()) {
            Collection<PsiAnnotation> customAnnotations = JavaAnnotationIndex.getInstance()
                .get(producerAnnotation.getShortName(), project, searchScope);
            for (PsiAnnotation annotation : customAnnotations) {
                PsiMethod producerMethod = getMethodFromAnnotation(annotation);
                if (isValidProducer(producerMethod)) {
                    allProducers.add(producerMethod);
                }
            }
        }
        
        return allProducers;
    }

    private static boolean isValidProducer(PsiMethod producerMethod) {
        return producerMethod != null && !PsiAnnotationHelper.isTestPackage(producerMethod.getContainingClass());
    }

    /**
     * 获取Producer的tag(支持多种注解方式)
     */
    private static String getProducerTag(PsiMethod method) {
        // @Deliver注解
        PsiAnnotation deliverAnnotation = method.getAnnotation(RocketMQDeliverAnnotation.PATH.getQualifiedName());
        if (deliverAnnotation != null) {
            String tag = getProducerTagFromAnnotation(deliverAnnotation);
            if (StringUtils.isNotBlank(tag)) {
                return tag;
            }
        }
        
        // 自定义Producer注解
        for (RocketMQProducerAnnotation producerAnnotation : RocketMQProducerAnnotation.getPathArray()) {
            PsiAnnotation annotation = method.getAnnotation(producerAnnotation.getQualifiedName());
            if (annotation != null) {
                String tag = getTagFromCustomProducerAnnotation(annotation);
                if (StringUtils.isNotBlank(tag)) {
                    return tag;
                }
            }
        }
        
        return "";
    }

    private static String getProducerTagFromAnnotation(PsiAnnotation annotation) {
        for (String attr : DELIVER_TAG_ATTRIBUTES) {
            String value = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, attr);
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trimToEmpty(value);
            }
        }
        return "";
    }
    
    private static String getTagFromCustomProducerAnnotation(PsiAnnotation annotation) {
        // 尝试嵌套属性
        for (String nestedAttr : CUSTOM_NESTED_ATTRIBUTES) {
            PsiAnnotationMemberValue value = annotation.findAttributeValue(nestedAttr);
            if (value instanceof PsiAnnotation nestedAnnotation) {
                String tag = PsiAnnotationHelper.getAnnotationAttributeValue(nestedAnnotation, "tag");
                if (StringUtils.isNotBlank(tag)) {
                    return StringUtils.trimToEmpty(tag);
                }
            }
        }
        
        // 尝试直接属性
        String directTag = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "tag");
        return StringUtils.isNotBlank(directTag) ? StringUtils.trimToEmpty(directTag) : "";
    }

    private static String getListenerTag(PsiAnnotation annotation) {
        String selectorExpression = PsiAnnotationHelper.getAnnotationAttributeValue(annotation, "selectorExpression");
        return StringUtils.trimToEmpty(selectorExpression);
    }

    private static PsiClass getClassFromAnnotation(PsiAnnotation annotation) {
        PsiElement parent = annotation.getParent();
        if (parent instanceof PsiModifierList) {
            PsiElement grandparent = parent.getParent();
            if (grandparent instanceof PsiClass) {
                return (PsiClass) grandparent;
            }
        }
        return null;
    }
    
    private static PsiMethod getMethodFromAnnotation(PsiAnnotation annotation) {
        PsiElement parent = annotation.getParent();
        if (parent instanceof PsiModifierList) {
            PsiElement grandparent = parent.getParent();
            if (grandparent instanceof PsiMethod) {
                return (PsiMethod) grandparent;
            }
        }
        return null;
    }

    private static PsiMethod findConsumeMethod(PsiClass clazz) {
        for (PsiMethod method : clazz.getMethods()) {
            if (CONSUME_METHOD_NAME.equals(method.getName())) {
                return method;
            }
        }
        return null;
    }

    private static boolean tagMatches(String tag1, String tag2) {
        if (StringUtils.isBlank(tag1) && StringUtils.isBlank(tag2)) {
            return true;
        }
        if (StringUtils.isBlank(tag1) || StringUtils.isBlank(tag2)) {
            return false;
        }
        
        // 支持通配符匹配
        if (WILDCARD_TAG.equals(tag1) || WILDCARD_TAG.equals(tag2)) {
            return true;
        }
        
        return tag1.equals(tag2);
    }
}
