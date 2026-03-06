package com.kaylves.interfacex.ui.form;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
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

    InterfaceItem interfaceItem;


    public RocketMQForm(InterfaceItem interfaceItem) {
        initUIContent(interfaceItem);

        reqBtn.addActionListener(new RocketmqActionListener(this));

        this.getRootPanel().setBorder(
                BorderFactory.createTitledBorder(
                        new MatteBorder(2,0,0,0, JBColor.YELLOW),
                        interfaceItem.getInterfaceItemCategoryEnum().name()));
    }

    private void initUIContent(InterfaceItem interfaceItem) {
        this.interfaceItem = interfaceItem;
        this.tagTxt.setText(interfaceItem.getUrl());
        setTopic(interfaceItem);
        setReqBody();
    }

    private void setTopic(InterfaceItem interfaceItem) {
        RocketMQItem rocketMQItem = (RocketMQItem) interfaceItem.getOriginalItem();
        this.topicTxt.setText(rocketMQItem.getTopic());
    }

    @Override
    public JPanel getPanel() {
        return this.getRootPanel();
    }

    public void setReqBody(){
        String requestBodyJson;
        PsiElement psiElement = interfaceItem.getPsiElement();
        if (psiElement.getLanguage() == JavaLanguage.INSTANCE) {
            PsiMethodHelper psiMethodHelper = PsiMethodHelper
                    .create(interfaceItem.getPsiMethod())
                    .withModule(interfaceItem.getModule());
            requestBodyJson = psiMethodHelper.buildRequestBodyJson();
            log.info("requestBodyJson:{}",requestBodyJson);
            this.bodyEditorPanel.setText(requestBodyJson);
        }
    }

    @Override
    public void flush(InterfaceItem interfaceItem) {
        initUIContent(interfaceItem);
    }

    @Override
    public InterfaceItemCategoryEnum getInterfaceXEnum() {
        return InterfaceItemCategoryEnum.RocketMQProducer;
    }
}
