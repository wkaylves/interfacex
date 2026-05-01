package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 简化的标签操作对话框
 */
@Slf4j
public class TagOperationDialog extends JDialog {
    
    private final Project project;
    private final SimpleTree simpleTree;
    private final InterfaceItem currentItem;
    
    private DefaultTableModel tableModel;
    private JTable tagTable;
    
    public TagOperationDialog(Project project, SimpleTree simpleTree, InterfaceItem currentItem) {
        super((Frame) null, "标签管理", true);
        this.project = project;
        this.simpleTree = simpleTree;
        this.currentItem = currentItem;
        
        initComponents();
        loadTags();
        
        setSize(600, 400);
        setLocationRelativeTo(null);
    }
    
    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // 顶部说明
        String interfaceInfo = currentItem != null ? 
            String.format("当前接口: %s", currentItem.getName()) : 
            "未选择接口";
        JLabel infoLabel = new JLabel(interfaceInfo);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD));
        mainPanel.add(infoLabel, BorderLayout.NORTH);
        
        // 中间表格
        String[] columns = {"标签名", "类型", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tagTable = new JTable(tableModel);
        tagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagTable.setRowHeight(30);
        
        JScrollPane scrollPane = new JScrollPane(tagTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        // 底部按钮
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        
        JButton addButton = new JButton("添加标签");
        addButton.addActionListener(e -> showAddTagDialog());
        buttonPanel.add(addButton);
        
        JButton applyButton = new JButton("应用到接口");
        applyButton.addActionListener(e -> applySelectedTag());
        buttonPanel.add(applyButton);
        
        JButton removeButton = new JButton("移除标签");
        removeButton.addActionListener(e -> removeSelectedTag());
        buttonPanel.add(removeButton);
        
        JButton deleteButton = new JButton("删除标签");
        deleteButton.addActionListener(e -> deleteSelectedTag());
        buttonPanel.add(deleteButton);
        
        JButton filterButton = new JButton("过滤");
        filterButton.addActionListener(e -> filterBySelectedTag());
        buttonPanel.add(filterButton);
        
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        buttonPanel.add(closeButton);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        setContentPane(mainPanel);
    }
    
    private void loadTags() {
        tableModel.setRowCount(0);
        
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        // 加载当前接口的标签
        List<String> currentTagNames = new java.util.ArrayList<>();
        if (currentItem != null) {
            List<TagEntity> currentTags = adapter.loadTagsByInterface(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
            );
            
            for (TagEntity tag : currentTags) {
                tableModel.addRow(new Object[]{tag.getTagName(), "✓ 已添加", ""});
                currentTagNames.add(tag.getTagName());
            }
        }
        
        // 加载其他标签
        List<TagEntity> allTags = adapter.loadTags(projectPath);
        List<String> allTagNames = allTags.stream()
            .map(TagEntity::getTagName)
            .distinct()
            .collect(Collectors.toList());
        
        for (String tagName : allTagNames) {
            if (!currentTagNames.contains(tagName)) {
                tableModel.addRow(new Object[]{tagName, "未添加", ""});
            }
        }
    }
    
    private void showAddTagDialog() {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tagName = JOptionPane.showInputDialog(this, "请输入标签名称:", "添加标签", JOptionPane.PLAIN_MESSAGE);
        if (tagName == null || tagName.trim().isEmpty()) {
            return;
        }
        
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        TagEntity tagEntity = TagEntity.builder()
            .projectPath(projectPath)
            .moduleName(currentItem.getModule() != null ? currentItem.getModule().getName() : "")
            .category(currentItem.getInterfaceItemCategoryEnum().name())
            .url(currentItem.getUrl())
            .httpMethod(currentItem.getMethod() != null ? currentItem.getMethod().name() : null)
            .methodName(currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "")
            .tagName(tagName.trim())
            .createdTime(System.currentTimeMillis())
            .updatedTime(System.currentTimeMillis())
            .build();
        
        adapter.saveTag(tagEntity);
        JOptionPane.showMessageDialog(this, "标签添加成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }
    
    private void applySelectedTag() {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        String tagStatus = (String) tableModel.getValueAt(selectedRow, 1);
        
        if ("✓ 已添加".equals(tagStatus)) {
            JOptionPane.showMessageDialog(this, "该标签已经添加到当前接口", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        TagEntity tagEntity = TagEntity.builder()
            .projectPath(projectPath)
            .moduleName(currentItem.getModule() != null ? currentItem.getModule().getName() : "")
            .category(currentItem.getInterfaceItemCategoryEnum().name())
            .url(currentItem.getUrl())
            .httpMethod(currentItem.getMethod() != null ? currentItem.getMethod().name() : null)
            .methodName(currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "")
            .tagName(tagName)
            .createdTime(System.currentTimeMillis())
            .updatedTime(System.currentTimeMillis())
            .build();
        
        adapter.saveTag(tagEntity);
        JOptionPane.showMessageDialog(this, "标签 \"" + tagName + "\" 已应用到当前接口", "成功", JOptionPane.INFORMATION_MESSAGE);
        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }
    
    private void removeSelectedTag() {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        String tagStatus = (String) tableModel.getValueAt(selectedRow, 1);
        
        if (!"✓ 已添加".equals(tagStatus)) {
            JOptionPane.showMessageDialog(this, "只能移除已添加到当前接口的标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要从当前接口移除标签 \"" + tagName + "\" 吗？",
            "确认移除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        adapter.deleteTag(
            projectPath,
            currentItem.getModule() != null ? currentItem.getModule().getName() : "",
            currentItem.getInterfaceItemCategoryEnum().name(),
            currentItem.getUrl(),
            currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
            currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "",
            tagName
        );
        
        JOptionPane.showMessageDialog(this, "标签移除成功！", "成功", JOptionPane.INFORMATION_MESSAGE);
        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }
    
    private void deleteSelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "确定要删除标签 \"" + tagName + "\" 吗？\n此操作将删除该标签与所有接口的关联。",
            "确认删除",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        List<TagEntity> tagsToDelete = adapter.loadTagsByTagName(projectPath, tagName);
        for (TagEntity tag : tagsToDelete) {
            adapter.deleteTag(
                projectPath,
                tag.getModuleName(),
                tag.getCategory(),
                tag.getUrl(),
                tag.getHttpMethod(),
                tag.getMethodName(),
                tag.getTagName()
            );
        }
        
        JOptionPane.showMessageDialog(this, "标签 \"" + tagName + "\" 已删除", "成功", JOptionPane.INFORMATION_MESSAGE);
        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
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
    
    public void showDialog() {
        setVisible(true);
    }
}
