package com.kaylves.interfacex.utils;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.javadoc.PsiDocComment;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.search.GlobalSearchScope;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;

/**
 * IDEA插件工具
 *
 * @author kaylves
 * @since 1.0.0
 */
public class IdeaPluginUtils {

    public static PsiClass findPsiClass(Project project, String qualifiedName, final GlobalSearchScope globalSearchScope) {
        return JavaPsiFacade.getInstance(project).findClass(qualifiedName, globalSearchScope);
    }

    public static File showFileChooser(AnActionEvent e, String filename, FileNameExtensionFilter fileNameExtensionFilter) {

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setSelectedFile(new File(filename));
        fileChooser.setFileFilter(fileNameExtensionFilter);

        int saveDialog = fileChooser.showSaveDialog(Objects.requireNonNull(e.getInputEvent()).getComponent());

        if (saveDialog == JFileChooser.APPROVE_OPTION) {
            return fileChooser.getSelectedFile();
        }

        return null;
    }

    /**
     * 获取方法拼接注释
     *
     * @param psiMethod {@link PsiMethod}
     * @return 返回获取方法拼接注释
     */
    public static String obtainDocAsString(PsiMethod psiMethod) {
        return obtainDocAsString(psiMethod.getDocComment());
    }

    /**
     * 获取方法拼接注释
     *
     * @param psiDocComment {@link PsiDocComment}
     * @return 返回获取方法拼接注释
     */
    public static String obtainDocAsString(PsiDocComment psiDocComment) {

        String docComment = "";

        if (psiDocComment != null) {
            StringBuilder sb = new StringBuilder();

            Arrays.stream(psiDocComment.getDescriptionElements()).forEach(psiElement1 -> sb.append(psiElement1.getText()));
            docComment = StringUtils.trimToEmpty(sb.toString());
        }

        if (StringUtils.isNotBlank(docComment)) {

            //特殊处理
            if (docComment.contains("<一句话功能简述>")) {
                docComment = docComment.substring(0, docComment.indexOf("<一句话功能简述>"));
            }

        }

        return docComment;
    }

    /**
     * 获取作者信息
     *
     * @param psiDocComment psiDocComment
     * @return 作者信息
     */
    public static String obtainAuth(PsiDocComment psiDocComment) {

        String author = "";
        if (psiDocComment == null) {
            return author;
        }

        PsiDocTag authorTag = psiDocComment.findTagByName("author");
        if (authorTag != null) {

            PsiDocTagValue psiDocTagValue = authorTag.getValueElement();
            if (psiDocTagValue == null) {
                return author;
            }

            return psiDocTagValue.getText();
        }

        return author;

    }

}
