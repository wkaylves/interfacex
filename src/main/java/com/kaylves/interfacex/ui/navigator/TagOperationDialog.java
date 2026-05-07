package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ColoredListCellRenderer;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.JBUI;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.ToolkitIcons;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class TagOperationDialog extends DialogWrapper {

    private static final Color[] TAG_PALETTE = {
            new JBColor(new Color(0x42, 0xA5, 0xF5), new Color(0x1E, 0x88, 0xE5)),
            new JBColor(new Color(0x66, 0xBB, 0x6A), new Color(0x43, 0xA0, 0x47)),
            new JBColor(new Color(0xFF, 0xA7, 0x26), new Color(0xFB, 0x8C, 0x00)),
            new JBColor(new Color(0xEF, 0x53, 0x50), new Color(0xE5, 0x39, 0x35)),
            new JBColor(new Color(0xAB, 0x47, 0xBC), new Color(0x8E, 0x24, 0xAA)),
            new JBColor(new Color(0x26, 0xA6, 0x9A), new Color(0x00, 0x89, 0x7B)),
            new JBColor(new Color(0x78, 0x90, 0x9C), new Color(0x54, 0x6E, 0x7A)),
            new JBColor(new Color(0xEC, 0x40, 0x7A), new Color(0xD8, 0x1B, 0x60)),
    };

    private final Project project;
    private final InterfaceItem currentItem;

    private JBTextField searchField;
    private JBTextField addField;
    private JBList<TagItem> tagList;
    private DefaultListModel<TagItem> listModel;
    private JBLabel statusLabel;
    private JLabel infoLabel;

    private List<TagItem> allTagItems = Collections.emptyList();

    public TagOperationDialog(Project project, com.intellij.ui.treeStructure.SimpleTree simpleTree, InterfaceItem currentItem) {
        super(project, false);
        this.project = project;
        this.currentItem = currentItem;

        setTitle("标签管理");
        init();
        loadTags();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout(8, 8));
        mainPanel.setPreferredSize(JBUI.size(520, 420));

        mainPanel.add(createInfoPanel(), BorderLayout.NORTH);
        mainPanel.add(createListPanel(), BorderLayout.CENTER);
        mainPanel.add(createAddPanel(), BorderLayout.SOUTH);

        return mainPanel;
    }

    @Override
    protected Action[] createActions() {
        return new Action[]{getCancelAction()};
    }

    @Override
    protected void createDefaultActions() {
        super.createDefaultActions();
    }

    @NotNull
    @Override
    protected DialogStyle getStyle() {
        return DialogStyle.COMPACT;
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 4));

        String interfaceInfo = currentItem != null
                ? currentItem.getName()
                : "未选择接口";
        infoLabel = new JLabel(ToolkitIcons.TAG);
        infoLabel.setText(" " + interfaceInfo);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD, 13));
        panel.add(infoLabel, BorderLayout.WEST);

        searchField = new JBTextField();
        searchField.putClientProperty("JComponent.placeholderText", "搜索标签...");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTags(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTags(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTags(); }
        });
        panel.add(searchField, BorderLayout.EAST);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        listModel = new DefaultListModel<>();
        tagList = new JBList<>(listModel);
        tagList.setCellRenderer(new TagItemRenderer());
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setFixedCellHeight(32);
        tagList.setVisibleRowCount(12);

        tagList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tagList.locationToIndex(e.getPoint());
                if (index < 0) return;
                Rectangle bounds = tagList.getCellBounds(index, index);
                if (bounds == null) return;

                TagItem item = listModel.getElementAt(index);
                int x = e.getX() - bounds.x;
                int width = bounds.width;

                if (x > width - 80) {
                    if (item.applied) {
                        removeTag(item);
                    } else {
                        applyTag(item);
                    }
                } else if (e.getClickCount() == 2) {
                    filterByTag(item);
                }
            }
        });

        tagList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    TagItem selected = tagList.getSelectedValue();
                    if (selected != null) {
                        deleteTag(selected);
                    }
                }
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(tagList);
        panel.add(scrollPane, BorderLayout.CENTER);

        statusLabel = new JBLabel(" ");
        statusLabel.setFont(statusLabel.getFont().deriveFont(Font.PLAIN, 11));
        statusLabel.setForeground(JBColor.GRAY);
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout(6, 0));
        panel.setBorder(new EmptyBorder(6, 0, 0, 0));

        addField = new JBTextField();
        addField.putClientProperty("JComponent.placeholderText", "输入新标签名，回车添加");
        addField.addActionListener(e -> addNewTag());

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> addNewTag());

        panel.add(addField, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.EAST);

        return panel;
    }

    private void loadTags() {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        Set<String> appliedTagNames = new HashSet<>();
        if (currentItem != null) {
            List<TagEntity> currentTags = adapter.loadTagsByInterface(
                    projectPath,
                    currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                    currentItem.getInterfaceItemCategoryEnum().name(),
                    currentItem.getUrl(),
                    currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                    currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
            );
            appliedTagNames = currentTags.stream()
                    .map(TagEntity::getTagName)
                    .collect(Collectors.toSet());
        }

        List<String> allTagNames = adapter.loadTags(projectPath).stream()
                .map(TagEntity::getTagName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<TagItem> items = new ArrayList<>();
        for (String tagName : allTagNames) {
            int count = adapter.loadTagsByTagName(projectPath, tagName).size();
            boolean applied = appliedTagNames.contains(tagName);
            items.add(new TagItem(tagName, count, applied));
        }

        allTagItems = items;
        applyFilter("");
    }

    private void filterTags() {
        String text = searchField.getText().trim().toLowerCase();
        applyFilter(text);
    }

    private void applyFilter(String filter) {
        listModel.clear();
        List<TagItem> applied = new ArrayList<>();
        List<TagItem> notApplied = new ArrayList<>();

        for (TagItem item : allTagItems) {
            if (!filter.isEmpty() && !item.tagName.toLowerCase().contains(filter)) {
                continue;
            }
            if (item.applied) {
                applied.add(item);
            } else {
                notApplied.add(item);
            }
        }

        if (!applied.isEmpty()) {
            listModel.addElement(new TagItem("── 已添加 ──", -1, true, true));
            for (TagItem item : applied) {
                listModel.addElement(item);
            }
        }

        if (!notApplied.isEmpty()) {
            listModel.addElement(new TagItem("── 可添加 ──", -1, false, true));
            for (TagItem item : notApplied) {
                listModel.addElement(item);
            }
        }

        if (listModel.isEmpty()) {
            listModel.addElement(new TagItem("暂无标签", -1, false, true));
        }

        updateStatus();
    }

    private void addNewTag() {
        String tagName = addField.getText().trim();
        if (tagName.isEmpty()) return;

        if (currentItem == null) {
            statusLabel.setText("请先选择一个接口");
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> existing = adapter.loadTagsByInterface(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
        );

        boolean alreadyExists = existing.stream().anyMatch(t -> t.getTagName().equals(tagName));
        if (alreadyExists) {
            statusLabel.setText("标签 '" + tagName + "' 已存在");
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
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        addField.setText("");
        statusLabel.setText("已添加标签: " + tagName);
        loadTags();
    }

    private void applyTag(TagItem item) {
        if (currentItem == null) {
            statusLabel.setText("请先选择一个接口");
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
                .tagName(item.tagName)
                .createdTime(System.currentTimeMillis())
                .updatedTime(System.currentTimeMillis())
                .build();

        adapter.saveTag(tagEntity);
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        statusLabel.setText("已应用标签: " + item.tagName);
        loadTags();
    }

    private void removeTag(TagItem item) {
        if (currentItem == null) return;

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        adapter.deleteTag(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "",
                item.tagName
        );

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        statusLabel.setText("已移除标签: " + item.tagName);
        loadTags();
    }

    private void deleteTag(TagItem item) {
        int choice = JOptionPane.showConfirmDialog(
                getRootPane(),
                "确定要删除标签 '" + item.tagName + "' 吗？\n这将删除所有接口上的此标签。",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (choice != JOptionPane.YES_OPTION) return;

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> tags = adapter.loadTagsByTagName(projectPath, item.tagName);
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

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        statusLabel.setText("已删除标签: " + item.tagName);
        loadTags();
    }

    private void filterByTag(TagItem item) {
        InterfaceXNavigator navigator = InterfaceXNavigator.getInstance(project);
        if (navigator != null) {
            navigator.applyTagFilter(item.tagName);
            close(OK_EXIT_CODE);
        }
    }

    private void updateStatus() {
        long appliedCount = allTagItems.stream().filter(i -> i.applied).count();
        long totalCount = allTagItems.size();
        statusLabel.setText("共 " + totalCount + " 个标签，当前接口已添加 " + appliedCount + " 个");
    }

    public void showDialog() {
        show();
    }

    private static class TagItem {
        final String tagName;
        final int usageCount;
        final boolean applied;
        final boolean separator;

        TagItem(String tagName, int usageCount, boolean applied) {
            this(tagName, usageCount, applied, false);
        }

        TagItem(String tagName, int usageCount, boolean applied, boolean separator) {
            this.tagName = tagName;
            this.usageCount = usageCount;
            this.applied = applied;
            this.separator = separator;
        }
    }

    private static Color getTagColor(String tagName) {
        int index = Math.abs(tagName.hashCode()) % TAG_PALETTE.length;
        return TAG_PALETTE[index];
    }

    private class TagItemRenderer extends ColoredListCellRenderer<TagItem> {
        private TagItem currentValue;

        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends TagItem> list,
                                              TagItem value, int index,
                                              boolean selected, boolean hasFocus) {
            this.currentValue = value;

            if (value.separator) {
                append(value.tagName);
                setBorder(JBUI.Borders.empty(4, 8));
                setForeground(JBColor.GRAY);
                setFont(getFont().deriveFont(Font.PLAIN, 11));
                return;
            }

            setBorder(JBUI.Borders.empty(4, 8));

            Color tagColor = getTagColor(value.tagName);

            append("● ");
            append(value.tagName);

            if (value.usageCount >= 0) {
                append("  (" + value.usageCount + ")", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, JBColor.GRAY));
            }

            if (value.applied) {
                append("  ✓", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_BOLD, tagColor));
            }

            setToolTipText(value.applied ? "点击右侧移除 | 双击过滤 | Delete 删除" : "点击右侧添加 | 双击过滤 | Delete 删除");
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            if (currentValue != null && !currentValue.separator) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int btnX = getWidth() - 76;
                int btnY = 6;
                int btnW = 68;
                int btnH = getHeight() - 12;

                Color tagColor = getTagColor(currentValue.tagName);
                if (currentValue.applied) {
                    g2.setColor(new JBColor(new Color(0xEF, 0x53, 0x50), new Color(0xE5, 0x39, 0x35)));
                } else {
                    g2.setColor(tagColor);
                }
                g2.fillRoundRect(btnX, btnY, btnW, btnH, 6, 6);

                g2.setColor(JBColor.WHITE);
                g2.setFont(g2.getFont().deriveFont(Font.PLAIN, 11));
                FontMetrics fm = g2.getFontMetrics();
                String btnText = currentValue.applied ? "移除" : "添加";
                int textX = btnX + (btnW - fm.stringWidth(btnText)) / 2;
                int textY = btnY + (btnH - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(btnText, textX, textY);

                g2.dispose();
            }
        }
    }
}
