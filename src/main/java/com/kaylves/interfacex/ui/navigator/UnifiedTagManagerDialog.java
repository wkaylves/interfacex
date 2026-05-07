package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class UnifiedTagManagerDialog extends JDialog {

    private final Project project;
    private final SimpleTree simpleTree;
    private final InterfaceItem currentItem;

    private JTable tagTable;
    private DefaultTableModel tableModel;
    private JTextArea detailArea;
    private JTextField newTagNameField;
    private JTextField newTagValueField;
    private JLabel selectedTagLabel;

    public UnifiedTagManagerDialog(Project project, SimpleTree simpleTree, InterfaceItem currentItem) {
        super((Frame) null, "标签管理", true);
        this.project = project;
        this.simpleTree = simpleTree;
        this.currentItem = currentItem;

        initComponents();
        loadTags();

        setSize(900, 600);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel leftPanel = createLeftPanel();
        JPanel rightPanel = createRightPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(350);
        splitPane.setResizeWeight(0.4);

        mainPanel.add(splitPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dispose());
        bottomPanel.add(closeButton);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createTitledBorder("标签列表"));

        String[] columns = {"标签名", "使用次数"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tagTable = new JTable(tableModel);
        tagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tagTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateDetailArea();
                }
            }
        });

        tagTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    filterBySelectedTag();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tagTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 10));

        JPanel addPanel = new JPanel(new BorderLayout(5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("添加新标签"));

        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        newTagNameField = new JTextField();
        newTagValueField = new JTextField();

        inputPanel.add(new JLabel("标签名:"));
        inputPanel.add(newTagNameField);
        inputPanel.add(new JLabel("标签值(可选):"));
        inputPanel.add(newTagValueField);

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> addNewTag());

        addPanel.add(inputPanel, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.EAST);

        panel.add(addPanel, BorderLayout.NORTH);

        JPanel detailPanel = new JPanel(new BorderLayout(5, 5));
        detailPanel.setBorder(BorderFactory.createTitledBorder("标签详情"));

        selectedTagLabel = new JLabel("请选择一个标签查看详细信息");
        selectedTagLabel.setFont(selectedTagLabel.getFont().deriveFont(Font.BOLD));

        detailArea = new JTextArea();
        detailArea.setEditable(false);
        detailArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane detailScroll = new JScrollPane(detailArea);

        detailPanel.add(selectedTagLabel, BorderLayout.NORTH);
        detailPanel.add(detailScroll, BorderLayout.CENTER);

        panel.add(detailPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));

        JButton applyButton = new JButton("应用到当前接口");
        applyButton.addActionListener(e -> applyToCurrentInterface());

        JButton removeButton = new JButton("从当前接口移除");
        removeButton.addActionListener(e -> removeFromCurrentInterface());

        JButton deleteButton = new JButton("删除标签");
        deleteButton.addActionListener(e -> deleteSelectedTag());

        JButton filterButton = new JButton("过滤视图");
        filterButton.addActionListener(e -> filterBySelectedTag());

        buttonPanel.add(applyButton);
        buttonPanel.add(removeButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(filterButton);

        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
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

    private void updateDetailArea() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            selectedTagLabel.setText("请选择一个标签查看详细信息");
            detailArea.setText("");
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        selectedTagLabel.setText("标签: " + tagName);

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> tags = adapter.loadTagsByTagName(projectPath, tagName);

        StringBuilder sb = new StringBuilder();
        sb.append("使用此标签的接口:\n\n");

        for (TagEntity tag : tags) {
            sb.append(String.format("• %s %s\n",
                    tag.getHttpMethod() != null ? tag.getHttpMethod() : "",
                    tag.getUrl()));
            sb.append(String.format("  模块: %s | 方法: %s\n\n",
                    tag.getModuleName(), tag.getMethodName()));
        }

        detailArea.setText(sb.toString());
        detailArea.setCaretPosition(0);
    }

    private void addNewTag() {
        String tagName = newTagNameField.getText().trim();
        if (tagName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入标签名",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<String> existingTags = adapter.loadTags(projectPath).stream()
                .map(TagEntity::getTagName)
                .distinct()
                .collect(Collectors.toList());

        if (existingTags.contains(tagName)) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "标签 '" + tagName + "' 已存在,是否继续添加?",
                    "标签已存在", JOptionPane.YES_NO_OPTION);
            if (choice != JOptionPane.YES_OPTION) {
                return;
            }
        }

        if (currentItem != null) {
            String tagValue = newTagValueField.getText().trim();
            TagEntity tagEntity = TagEntity.builder()
                    .projectPath(projectPath)
                    .moduleName(currentItem.getModule() != null ? currentItem.getModule().getName() : "")
                    .category(currentItem.getInterfaceItemCategoryEnum().name())
                    .url(currentItem.getUrl())
                    .httpMethod(currentItem.getMethod() != null ? currentItem.getMethod().name() : null)
                    .methodName(currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "")
                    .tagName(tagName)
                    .tagValue(tagValue.isEmpty() ? null : tagValue)
                    .createdTime(System.currentTimeMillis())
                    .updatedTime(System.currentTimeMillis())
                    .build();

            adapter.saveTag(tagEntity);
            JOptionPane.showMessageDialog(this, "标签添加成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "标签已创建,请在接口上右键添加此标签",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
        }

        newTagNameField.setText("");
        newTagValueField.setText("");
        loadTags();
    }

    private void applyToCurrentInterface() {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先在树中选择一个接口",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> existingTags = adapter.loadTagsByInterface(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
        );

        boolean alreadyExists = existingTags.stream()
                .anyMatch(t -> t.getTagName().equals(tagName));

        if (alreadyExists) {
            JOptionPane.showMessageDialog(this, "该接口已有此标签",
                    "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

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
        JOptionPane.showMessageDialog(this, "标签应用成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        updateDetailArea();
    }

    private void removeFromCurrentInterface() {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先在树中选择一个接口",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);

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

        JOptionPane.showMessageDialog(this, "标签移除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        updateDetailArea();
    }

    private void deleteSelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签",
                    "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);

        int choice = JOptionPane.showConfirmDialog(this,
                "确定要删除标签 '" + tagName + "' 吗?\n这将删除所有接口上的此标签。",
                "确认删除", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (choice != JOptionPane.YES_OPTION) {
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> tags = adapter.loadTagsByTagName(projectPath, tagName);
        for (TagEntity tag : tags) {
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

        JOptionPane.showMessageDialog(this, "标签删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        detailArea.setText("");
        selectedTagLabel.setText("请选择一个标签查看详细信息");
    }

    private void filterBySelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "请先选择一个标签",
                    "提示", JOptionPane.WARNING_MESSAGE);
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
