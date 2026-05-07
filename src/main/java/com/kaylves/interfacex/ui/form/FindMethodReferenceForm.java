package com.kaylves.interfacex.ui.form;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class FindMethodReferenceForm {
    private JPanel rootPanel;
    private JTextField textField1;
    private JButton button1;
    private JTextArea textArea1;

    public FindMethodReferenceForm() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                ProgressManager.getInstance().run(new Task.Backgroundable(null, "Find Method Reference", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {

                    }
                });
            }
        });
    }
}
