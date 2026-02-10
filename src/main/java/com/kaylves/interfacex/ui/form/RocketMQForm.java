package com.kaylves.interfacex.ui.form;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.module.rocketmq.RocketMQItem;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.utils.PsiMethodHelper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.MatteBorder;

@Getter
@Slf4j
public class RocketMQForm implements InterfaceXForm {
    private JTextField serverTxt;
    private JButton reqBtn;
    private JTextField topicTxt;
    private JTextField tagTxt;
    private JTabbedPane tabbedPanel;
    private JPanel bodyTabPanel;
    private JPanel resTabPanel;
    private JPanel rootPanel;
    private JEditorPane bodyEditorPanel;
    private JEditorPane resultEditorPanel;

    InterfaceXItem interfaceXItem;


    public RocketMQForm(InterfaceXItem interfaceXItem) {
        initUIContent(interfaceXItem);

        reqBtn.addActionListener(new RocketmqActionListener(this));

        this.getRootPanel().setBorder(
                BorderFactory.createTitledBorder(
                        new MatteBorder(2,0,0,0, JBColor.YELLOW),
                        interfaceXItem.getInterfaceXItemCategoryEnum().name()));
    }

    private void initUIContent(InterfaceXItem interfaceXItem) {
        this.interfaceXItem = interfaceXItem;
        this.tagTxt.setText(interfaceXItem.getUrl());
        setTopic(interfaceXItem);
        setReqBody();
    }

    private void setTopic(InterfaceXItem interfaceXItem) {
        RocketMQItem rocketMQItem = (RocketMQItem) interfaceXItem.getOriginalItem();
        this.topicTxt.setText(rocketMQItem.getTopic());
    }

    @Override
    public JPanel getPanel() {
        return this.getRootPanel();
    }

    public void setReqBody(){
        String requestBodyJson;
        PsiElement psiElement = interfaceXItem.getPsiElement();
        if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
            PsiMethodHelper psiMethodHelper = PsiMethodHelper
                    .create(interfaceXItem.getPsiMethod())
                    .withModule(interfaceXItem.getModule());
            requestBodyJson = psiMethodHelper.buildRequestBodyJson();
            log.info("requestBodyJson:{}",requestBodyJson);
            this.bodyEditorPanel.setText(requestBodyJson);
        }
    }

    @Override
    public void flush(InterfaceXItem interfaceXItem) {
        initUIContent(interfaceXItem);
    }

    @Override
    public InterfaceXItemCategoryEnum getInterfaceXEnum() {
        return InterfaceXItemCategoryEnum.RocketMQProducer;
    }
}
