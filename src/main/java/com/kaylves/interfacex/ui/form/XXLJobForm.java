package com.kaylves.interfacex.ui.form;

import com.intellij.ui.JBColor;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.common.InterfaceItem;
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
    private JPanel tabPanel2;
    private JTextArea paramTxtArea;

    private final InterfaceItem interfaceItem;

    public XXLJobForm(InterfaceItem interfaceItem) {
        this.interfaceItem = interfaceItem;
        this.addressTxt.setText("http://localhost:11813/");
        this.handlerTxt.setText(interfaceItem.getUrl());
        this.getRootPanel().setBorder(BorderFactory.createTitledBorder(new MatteBorder(2,0,0,0, JBColor.YELLOW), interfaceItem.getInterfaceItemCategoryEnum().name()));
        reqBtn.addActionListener(new XXLJobActionListener(this));
    }

    @Override
    public JPanel getPanel() {
        return this.rootPanel;
    }

    @Override
    public void flush(InterfaceItem interfaceItem) {
        this.handlerTxt.setText(interfaceItem.getUrl());
    }

    @Override
    public InterfaceItemCategoryEnum getInterfaceXEnum() {
        return InterfaceItemCategoryEnum.XXLJob;
    }
}
