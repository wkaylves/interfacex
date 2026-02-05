package com.kaylves.interfacex.ui.form;

import com.intellij.ui.JBColor;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.common.InterfaceXItem;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.MatteBorder;
@Slf4j
@Getter
public class XXLJobForm implements InterfaceXForm {
    private JTextField addressTxt;
    private JButton reqBtn;
    private JLabel addressLabel;
    private JLabel handler;
    private JTextField handlerTxt;
    private JEditorPane resultEditorPane;
    private JPanel rootPanel;
    private JTabbedPane tabbedPane1;
    private JPanel tapPanel1;
    private JPanel tabPannel2;
    private JTextArea paramTxtArea;
    private InterfaceXItem interfaceXItem;

    public XXLJobForm(InterfaceXItem interfaceXItem) {
        this.interfaceXItem = interfaceXItem;
        this.addressTxt.setText("http://localhost:11813/");
        this.handlerTxt.setText(interfaceXItem.getUrl());
        this.getRootPanel().setBorder(BorderFactory.createTitledBorder(new MatteBorder(2,0,0,0, JBColor.YELLOW), interfaceXItem.getInterfaceXItemCategoryEnum().name()));
        reqBtn.addActionListener(new XXLJobActionListener(this));
    }

    @Override
    public JPanel getPanel() {
        return this.rootPanel;
    }

    @Override
    public void flush(InterfaceXItem interfaceXItem) {
        this.handlerTxt.setText(interfaceXItem.getUrl());
    }

    @Override
    public InterfaceXItemCategoryEnum getInterfaceXEnum() {
        return InterfaceXItemCategoryEnum.XXLJob;
    }
}
