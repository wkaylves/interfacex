package com.kaylves.interfacex.ui.form;

import lombok.Getter;
import lombok.Setter;

import javax.swing.*;

@Getter
@Setter
public class XXLJobForm {
    private JPanel rootPannel;
    private JTextField addressText;
    private JTextField portText;
    private JTextArea resutTextArea;
    private JLabel handleValue;

    private String jobName;

    public XXLJobForm(String jobName) {
        this.jobName = jobName;
        this.handleValue.setText(jobName);
        this.addressText.setText("http://127.0.0.1:11813/");
    }

    public JComponent getRootPanel() {
        return rootPannel;
    }
}
