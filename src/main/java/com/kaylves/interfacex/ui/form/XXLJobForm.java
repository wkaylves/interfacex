package com.kaylves.interfacex.ui.form;

import com.intellij.ui.JBColor;
import com.kaylves.interfacex.ui.navigator.ServiceItem;
import lombok.Getter;

import javax.swing.*;
import javax.swing.border.MatteBorder;

@Getter
public class XXLJobForm {
    private JTextField addressTxt;
    private JButton reqBtn;
    private JLabel addressLabel;
    private JLabel handler;
    private JTextField handlerTxt;
    private JEditorPane resultEditorPane;
    private JPanel rootPanel;
    private ServiceItem serviceItem;

    public XXLJobForm(ServiceItem serviceItem) {
        this.serviceItem = serviceItem;
        this.addressTxt.setText("http://localhost:8080/");
        this.handlerTxt.setText(serviceItem.getUrl());
        this.getRootPanel().setBorder(BorderFactory.createTitledBorder(new MatteBorder(2,0,0,0, JBColor.YELLOW), serviceItem.getInterfaceXEnum().name()));
        reqBtn.addActionListener(new XXLJobActionListener(this));
    }
}
