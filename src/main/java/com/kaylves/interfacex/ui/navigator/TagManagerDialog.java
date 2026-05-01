package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TagManagerDialog {

    private final Project project;
    private final InterfaceItem currentItem;
    private final JDialog dialog;

    private DefaultTableModel tableModel;
    private JTable tagTable;
    private JTextField searchField;
    private JLabel statusLabel;

    public TagManagerDialog(Project project, InterfaceItem currentItem) {
        this.project = project;
        this.currentItem = currentItem;
        this.dialog = createDialog();
    }

    private JDialog createDialog() {
        JDialog dialog = new JDialog((Frame) null, "标签管理", true);
        dialog.setContentPane(createMainPanel());
        dialog.setSize(1000, 650);
        dialog.setLocationRelativeTo(null);
        return dialog;
    }

    private JPanel createMainPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 5));

        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        JLabel searchLabel = new JLabel("搜索:");
        searchField = new JBTextField();
        searchField.putClientProperty("JComponent.placeholderText", "输入标签名过滤...");
        
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterTags();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterTags();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterTags();
            }
        });

        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadTags());
        refreshButton.setToolTipText("重新加载标签列表");

        topPanel.add(searchPanel, BorderLayout.CENTER);
        topPanel.add(refreshButton, BorderLayout.EAST);

        return topPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout(5, 5));

        String[] columns = {"标签名", "使用次数", "操作"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 2;
            }
        };

        tagTable = new JTable(tableModel);
        tagTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagTable.setRowHeight(32);
        tagTable.getTableHeader().setReorderingAllowed(false);
        tagTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        tagTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        tagTable.getColumnModel().getColumn(2).setPreferredWidth(150);

        tagTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = tagTable.rowAtPoint(e.getPoint());
                int col = tagTable.columnAtPoint(e.getPoint());
                
                if (row >= 0 && col == 2) {
                    String tagName = (String) tableModel.getValueAt(row, 0);
                    handleTagAction(tagName);
                }
            }
        });

        tagTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateStatus();
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(tagTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        centerPanel.add(scrollPane, BorderLayout.CENTER);

        return centerPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new BorderLayout(10, 5));

        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.GRAY);
        bottomPanel.add(statusLabel, BorderLayout.WEST);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton addTagButton = new JButton("新建标签");
        addTagButton.addActionListener(e -> showAddTagDialog());
        addTagButton.setToolTipText("创建新标签并添加到当前接口");

        JButton applyTagButton = new JButton("应用选中");
        applyTagButton.addActionListener(e -> applySelectedTag());
        applyTagButton.setToolTipText("将选中的标签应用到当前接口");

        JButton removeTagButton = new JButton("移除标签");
        removeTagButton.addActionListener(e -> removeTagFromInterface());
        removeTagButton.setToolTipText("从当前接口移除选中的标签");

        JButton filterButton = new JButton("过滤视图");
        filterButton.addActionListener(e -> filterBySelectedTag());
        filterButton.setToolTipText("只显示带有此标签的接口");

        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(addTagButton);
        buttonPanel.add(applyTagButton);
        buttonPanel.add(removeTagButton);
        buttonPanel.add(filterButton);
        buttonPanel.add(closeButton);

        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        return bottomPanel;
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
            tableModel.addRow(new Object[]{tagName, count, createActionButton(tagName)});
        }

        updateStatus();
    }

    private JButton createActionButton(String tagName) {
        JButton button = new JButton("管理");
        button.setFont(button.getFont().deriveFont(Font.PLAIN, 11));
        button.addActionListener(e -> handleTagAction(tagName));
        return button;
    }

    private void filterTags() {
        String filterText = searchField.getText().trim().toLowerCase();
        
        loadTagsWithFilter(filterText);
    }
    
    private void loadTagsWithFilter(String filterText) {
        tableModel.setRowCount(0);

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> allTags = adapter.loadTags(projectPath);
        List<String> tagNames = allTags.stream()
                .map(TagEntity::getTagName)
                .distinct()
                .collect(Collectors.toList());

        for (String tagName : tagNames) {
            if (filterText.isEmpty() || tagName.toLowerCase().contains(filterText)) {
                int count = adapter.loadTagsByTagName(projectPath, tagName).size();
                tableModel.addRow(new Object[]{tagName, count, createActionButton(tagName)});
            }
        }

        updateStatus();
    }

    private void handleTagAction(String tagName) {
        String[] options = {"查看详情", "应用到当前接口", "从当前接口移除", "删除标签", "取消"};
        int choice = JOptionPane.showOptionDialog(
                dialog,
                "对标签 \"" + tagName + "\" 执行什么操作？",
                "标签操作",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]
        );

        switch (choice) {
            case 0:
                showTagDetail(tagName);
                break;
            case 1:
                applyTag(tagName);
                break;
            case 2:
                removeTag(tagName);
                break;
            case 3:
                deleteTag(tagName);
                break;
        }
    }

    private void showTagDetail(String tagName) {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> tags = adapter.loadTagsByTagName(projectPath, tagName);

        StringBuilder detailText = new StringBuilder();
        detailText.append("标签名称：").append(tagName).append("\n");
        detailText.append("使用次数：").append(tags.size()).append("\n\n");
        detailText.append("使用该标签的接口:\n\n");

        for (TagEntity tag : tags) {
            detailText.append("• ").append(tag.getHttpMethod() != null ? tag.getHttpMethod() : "")
                    .append(" ").append(tag.getUrl()).append("\n");
            detailText.append("  模块：").append(tag.getModuleName())
                    .append(" | 方法：").append(tag.getMethodName()).append("\n\n");
        }

        JTextArea textArea = new JTextArea(detailText.toString());
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setCaretPosition(0);

        JBScrollPane scrollPane = new JBScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(600, 400));

        JOptionPane.showMessageDialog(dialog, scrollPane, "标签详情", JOptionPane.INFORMATION_MESSAGE);
    }

    private void showAddTagDialog() {
        DialogBuilder builder = new DialogBuilder(project);
        builder.setTitle("新建标签");

        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField tagNameField = new JTextField();
        JTextField tagValueField = new JTextField();

        panel.add(new JLabel("标签名:"));
        panel.add(tagNameField);
        panel.add(new JLabel("标签值 (可选):"));
        panel.add(tagValueField);

        builder.setCenterPanel(panel);
        builder.setTitle("新建标签");

        DialogBuilder dialogBuilder = builder;
        builder.addOkAction();
        builder.addCancelAction();

        int result = builder.show();
        
        if (result == 0) {
            String tagName = tagNameField.getText().trim();
            if (tagName.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "请输入标签名", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String tagValue = tagValueField.getText().trim();
            
            if (currentItem != null) {
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
                        .tagValue(tagValue.isEmpty() ? null : tagValue)
                        .createdTime(System.currentTimeMillis())
                        .updatedTime(System.currentTimeMillis())
                        .build();

                adapter.saveTag(tagEntity);
                JOptionPane.showMessageDialog(dialog, "标签创建成功!", "成功", JOptionPane.INFORMATION_MESSAGE);
                
                InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
            } else {
                JOptionPane.showMessageDialog(dialog, 
                        "标签已创建，请在接口上右键添加此标签",
                        "提示", JOptionPane.INFORMATION_MESSAGE);
            }

            loadTags();
        }
    }

    private void applySelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        applyTag(tagName);
    }

    private void applyTag(String tagName) {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(dialog, "请先在树中选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

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
            JOptionPane.showMessageDialog(dialog, "该接口已有此标签", "提示", JOptionPane.INFORMATION_MESSAGE);
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
        JOptionPane.showMessageDialog(dialog, "标签应用成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    private void removeTagFromInterface() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);
        removeTag(tagName);
    }

    private void removeTag(String tagName) {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(dialog, "请先在树中选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
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

        JOptionPane.showMessageDialog(dialog, "标签移除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    private void deleteTag(String tagName) {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int choice = JOptionPane.showConfirmDialog(dialog,
                "确定要删除标签 '" + tagName + "' 吗？\n这将删除所有接口上的此标签。",
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

        JOptionPane.showMessageDialog(dialog, "标签删除成功!", "成功", JOptionPane.INFORMATION_MESSAGE);

        loadTags();
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    private void filterBySelectedTag() {
        int selectedRow = tagTable.getSelectedRow();
        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(dialog, "请先选择一个标签", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tagName = (String) tableModel.getValueAt(selectedRow, 0);

        InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);
        if (navigator != null) {
            navigator.applyTagFilter(tagName);
            dialog.dispose();
        }
    }

    private void updateStatus() {
        int selectedRow = tagTable.getSelectedRow();
        int totalTags = tableModel.getRowCount();

        if (selectedRow >= 0) {
            String tagName = (String) tableModel.getValueAt(selectedRow, 0);
            int count = (int) tableModel.getValueAt(selectedRow, 1);
            statusLabel.setText("已选择：" + tagName + " (被 " + count + " 个接口使用)");
        } else {
            statusLabel.setText("共 " + totalTags + " 个标签");
        }
    }

    public void show() {
        loadTags();
        dialog.setVisible(true);
    }
    
    public void dispose() {
        dialog.dispose();
    }
}
