package com.kaylves.interfacex.ui.form;


import com.intellij.ui.JBColor;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.common.InterfaceXItem;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.MatteBorder;

@Getter
public class RocketMQForm implements InterfaceXForm {

    private JPanel rootPanel;
    private JTextField serverTxt;
    private JButton reqBtn;
    private JEditorPane bodyEditorPanel;
    private JTextField topicTxt;
    private JTextField tagTxt;
    private JTabbedPane tabPanel;
    private JPanel bodyTabPanel;
    private JEditorPane resultEditorPanel;
    private JPanel resTabPanel;

    InterfaceXItem interfaceXItem;

    public RocketMQForm(InterfaceXItem interfaceXItem) {
        this.interfaceXItem = interfaceXItem;
        this.tagTxt.setText(interfaceXItem.getUrl());
        reqBtn.addActionListener(new RocketmqActionListener(this));
        this.getRootPanel().setBorder(BorderFactory.createTitledBorder(new MatteBorder(2,0,0,0, JBColor.YELLOW), interfaceXItem.getInterfaceXItemCategoryEnum().name()));
    }

    @Override
    public JPanel getPanel() {
        return this.getRootPanel();
    }

    @Override
    public void flush(InterfaceXItem interfaceXItem) {
        this.tagTxt.setText(interfaceXItem.getUrl());
    }

    @Override
    public InterfaceXItemCategoryEnum getInterfaceXEnum() {
        return InterfaceXItemCategoryEnum.RocketMQProducer;
    }
}
