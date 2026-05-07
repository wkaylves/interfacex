package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class GlobalTagManagerDialog extends JDialog {

    private final Project project;
    private final JTable tagTable;
    private final DefaultTableModel tableModel;

    public GlobalTagManagerDialog(Project project) {
        super((Frame) null, "全局标签管理", true);
        this.project = project;

        String[] columns = {"标签名", "使用次数"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tagTable = new JTable(tableModel);
        tagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        initComponents();
        loadTags();

        setSize(600, 400);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(tagTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton filterButton = new JButton("过滤");
        filterButton.addActionListener(e -> filterBySelectedTag());

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());

        buttonPanel.add(filterButton);
        buttonPanel.add(closeButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private void loadTags() {
        tableModel.setRowCount(0);

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> allTags = adapter.loadTags(projectPath);
        List<String> tagNames = allTags.stream()
                .map(TagEntity::getTagName)
                .distinct()
                .collect(Collectors.toList());

        for (String tagName : tagNames) {
            int count = adapter.loadTagsByTagName(projectPath, tagName).size();
            tableModel.addRow(new Object[]{tagName, count});
        }
    }

    private void filterBySelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);

        InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);
        if (navigator != null) {
            navigator.applyTagFilter(tagName);
            dispose();
        }
    }
}
