package com.kaylves.interfacex.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.JBScrollPane;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TagQuickSelectorPanel extends JPanel {

    private final Project project;
    private final InterfaceItem currentItem;
    private final Runnable onTagChanged;

    private JPanel tagsPanel;
    private JLabel noTagsLabel;

    public TagQuickSelectorPanel(Project project, InterfaceItem currentItem, Runnable onTagChanged) {
        super(new BorderLayout(5, 5));
        this.project = project;
        this.currentItem = currentItem;
        this.onTagChanged = onTagChanged;

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        initComponents();
        loadTags();
    }

    private void initComponents() {
        JLabel titleLabel = new JLabel("标签:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));

        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 5));
        tagsPanel.setOpaque(false);

        noTagsLabel = new JLabel("暂无标签，点击 + 添加");
        noTagsLabel.setForeground(Color.GRAY);
        noTagsLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        noTagsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                openTagManager();
            }
        });

        JButton addButton = new JButton("+");
        addButton.setFont(addButton.getFont().deriveFont(Font.BOLD, 14));
        addButton.setToolTipText("添加标签");
        addButton.addActionListener(e -> openTagManager());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(addButton);

        JScrollPane scrollPane = new JBScrollPane(tagsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        JPanel centerPanel = new JPanel(new BorderLayout(5, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(buttonPanel, BorderLayout.EAST);

        add(titleLabel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadTags() {
        tagsPanel.removeAll();

        if (currentItem == null) {
            noTagsLabel.setText("请先选择一个接口");
            tagsPanel.add(noTagsLabel);
            revalidate();
            repaint();
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> tags = adapter.loadTagsByInterface(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
        );

        if (tags.isEmpty()) {
            tagsPanel.add(noTagsLabel);
        } else {
            for (TagEntity tag : tags) {
                TagLabel tagLabel = new TagLabel(tag);
                tagsPanel.add(tagLabel);
            }
        }

        revalidate();
        repaint();
    }

    private void openTagManager() {
        JDialog dialog = new JDialog((Frame) null, "标签管理", true);
        dialog.setSize(500, 400);
        dialog.setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        String[] existingTags = getExistingTagNames();
        
        if (existingTags.length > 0) {
            JPanel selectPanel = new JPanel(new BorderLayout(5, 5));
            selectPanel.setBorder(BorderFactory.createTitledBorder("快速选择"));
            
            JComboBox<String> tagComboBox = new JComboBox<>(existingTags);
            tagComboBox.setEditable(false);
            tagComboBox.setMaximumRowCount(10);
            
            JButton selectButton = new JButton("添加选中");
            selectButton.addActionListener(e -> {
                String selectedTag = (String) tagComboBox.getSelectedItem();
                if (selectedTag != null) {
                    addTagToCurrent(selectedTag);
                    dialog.dispose();
                    loadTags();
                    if (onTagChanged != null) {
                        onTagChanged.run();
                    }
                }
            });
            
            selectPanel.add(tagComboBox, BorderLayout.CENTER);
            selectPanel.add(selectButton, BorderLayout.EAST);
            mainPanel.add(selectPanel, BorderLayout.NORTH);
        }

        JPanel addPanel = new JPanel(new BorderLayout(5, 5));
        addPanel.setBorder(BorderFactory.createTitledBorder("新建标签"));
        
        JPanel inputPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        JTextField newTagNameField = new JTextField();
        JTextField newTagValueField = new JTextField();
        
        inputPanel.add(new JLabel("标签名:"));
        inputPanel.add(newTagNameField);
        inputPanel.add(new JLabel("标签值 (可选):"));
        inputPanel.add(newTagValueField);
        
        JButton addButton = new JButton("创建并添加");
        addButton.addActionListener(e -> {
            String tagName = newTagNameField.getText().trim();
            if (tagName.isEmpty()) {
                JOptionPane.showMessageDialog(mainPanel, "请输入标签名", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            String tagValue = newTagValueField.getText().trim();
            addTagToCurrent(tagName, tagValue.isEmpty() ? null : tagValue);
            dialog.dispose();
            loadTags();
            if (onTagChanged != null) {
                onTagChanged.run();
            }
        });
        
        addPanel.add(inputPanel, BorderLayout.CENTER);
        addPanel.add(addButton, BorderLayout.EAST);
        
        mainPanel.add(addPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dialog.dispose());
        bottomPanel.add(cancelButton);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        dialog.setContentPane(mainPanel);
        dialog.setVisible(true);
    }

    private String[] getExistingTagNames() {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();
        
        List<TagEntity> allTags = adapter.loadTags(projectPath);
        return allTags.stream()
                .map(TagEntity::getTagName)
                .distinct()
                .toArray(String[]::new);
    }

    private void addTagToCurrent(String tagName) {
        addTagToCurrent(tagName, null);
    }

    private void addTagToCurrent(String tagName, String tagValue) {
        if (currentItem == null) {
            JOptionPane.showMessageDialog(this, "请先选择一个接口", "提示", JOptionPane.WARNING_MESSAGE);
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
                .tagValue(tagValue)
                .createdTime(System.currentTimeMillis())
                .updatedTime(System.currentTimeMillis())
                .build();

        adapter.saveTag(tagEntity);
        
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    private class TagLabel extends JPanel {
        private final TagEntity tag;

        public TagLabel(TagEntity tag) {
            this.tag = tag;
            setLayout(new BorderLayout(3, 0));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(getTagColor(tag.getTagName()), 1),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
            setBackground(getTagColor(tag.getTagName()).brighter().brighter());
            setOpaque(true);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(tag.getTagName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 11));
            add(nameLabel, BorderLayout.CENTER);

            if (tag.getTagValue() != null && !tag.getTagValue().isEmpty()) {
                JLabel valueLabel = new JLabel("=" + tag.getTagValue());
                valueLabel.setFont(valueLabel.getFont().deriveFont(Font.ITALIC, 10));
                valueLabel.setForeground(Color.GRAY);
                add(valueLabel, BorderLayout.EAST);
            }

            JButton removeButton = new JButton("×");
            removeButton.setFont(removeButton.getFont().deriveFont(Font.BOLD, 10));
            removeButton.setBorderPainted(false);
            removeButton.setContentAreaFilled(false);
            removeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            removeButton.setToolTipText("移除标签");
            removeButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    removeButton.setOpaque(true);
                    removeButton.setBackground(Color.RED);
                    removeButton.setForeground(Color.WHITE);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    removeButton.setOpaque(false);
                    repaint();
                }
            });
            removeButton.addActionListener(e -> removeTag());

            add(removeButton, BorderLayout.EAST);

            setToolTipText(tag.getTagName() + 
                    (tag.getTagValue() != null ? "=" + tag.getTagValue() : ""));
        }

        private Color getTagColor(String tagName) {
            int hashCode = tagName.hashCode();
            int r = (hashCode & 0xFF0000) >> 16;
            int g = (hashCode & 0x00FF00) >> 8;
            int b = hashCode & 0x0000FF;
            
            r = 150 + (r % 106);
            g = 150 + (g % 106);
            b = 150 + (b % 106);
            
            return new Color(r, g, b);
        }

        private void removeTag() {
            int result = JOptionPane.showConfirmDialog(
                    this,
                    "确定要移除标签 \"" + tag.getTagName() + "\" 吗？",
                    "确认移除",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE
            );

            if (result != JOptionPane.YES_OPTION) {
                return;
            }

            StorageAdapter adapter = StorageAdapter.getInstance();
            String projectPath = project.getBasePath();

            adapter.deleteTag(
                    projectPath,
                    tag.getModuleName(),
                    tag.getCategory(),
                    tag.getUrl(),
                    tag.getHttpMethod(),
                    tag.getMethodName(),
                    tag.getTagName()
            );

            loadTags();
            
            InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
            
            if (onTagChanged != null) {
                onTagChanged.run();
            }
        }
    }
}
