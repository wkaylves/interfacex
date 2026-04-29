package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.kaylves.interfacex.db.InterfaceXDatabaseService;
import com.kaylves.interfacex.db.dao.TagDao;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/**
 * 全局标签管理对话框
 */
@Slf4j
public class GlobalTagManagerDialog extends JDialog {

    private final Project project;
    private final JTable tagTable;
    private final DefaultTableModel tableModel;

    public GlobalTagManagerDialog(Project project) {
        super((Frame) null, "全局标签管理", true);
        this.project = project;
        
        // 创建表格模型
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
        
        // 表格面板
        JScrollPane scrollPane = new JScrollPane(tagTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 按钮面板
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
        
        try {
            InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
            TagDao tagDao = dbService.getTagDao();
            String projectPath = project.getBasePath();
            
            List<String> tagNames = tagDao.findAllTagNames(projectPath);
            
            for (String tagName : tagNames) {
                // 统计该标签的使用次数
                int count = countTagUsage(projectPath, tagName);
                tableModel.addRow(new Object[]{tagName, count});
            }
        } catch (SQLException e) {
            log.error("Failed to load tags", e);
            JOptionPane.showMessageDialog(this, "加载标签失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int countTagUsage(String projectPath, String tagName) throws SQLException {
        InterfaceXDatabaseService dbService = InterfaceXDatabaseService.getInstance();
        TagDao tagDao = dbService.getTagDao();
        return tagDao.findByTagName(projectPath, tagName).size();
    }

    private void filterBySelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        
        // 获取树结构并应用过滤
        InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);
        if (navigator != null) {
            navigator.applyTagFilter(tagName);
            dispose();
        }
    }
}
