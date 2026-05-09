package com.kaylves.interfacex.ui.navigator;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
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

    private static final int MAX_TAG_DISPLAY_LEN = 20;

    private final Project project;
    private final List<InterfaceItem> items;

    private JBTextField searchField;
    private JBLabel searchLabel;
    private JBTextField addField;
    private JList<TagItem> tagList;
    private DefaultListModel<TagItem> listModel;
    private JBLabel statusLabel;
    private JLabel infoLabel;

    private List<TagItem> allTagItems = Collections.emptyList();

    /**
     * 构造函数 - 支持单个接口（兼容旧调用）
     */
    public TagOperationDialog(Project project, com.intellij.ui.treeStructure.SimpleTree simpleTree, InterfaceItem currentItem) {
        this(project, simpleTree, currentItem != null ? List.of(currentItem) : Collections.emptyList());
    }

    /**
     * 构造函数 - 支持多个接口（批量标签）
     */
    public TagOperationDialog(Project project, com.intellij.ui.treeStructure.SimpleTree simpleTree, List<InterfaceItem> items) {
        super(project, false);
        this.project = project;
        this.items = items != null ? new ArrayList<>(items) : Collections.emptyList();

        setTitle(items.size() > 1 ? "标签管理 - " + items.size() + " 个接口" : "标签管理");
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

        String interfaceInfo;
        if (items.isEmpty()) {
            interfaceInfo = "未选择接口";
        } else if (items.size() == 1) {
            interfaceInfo = items.get(0).getName();
        } else {
            interfaceInfo = items.size() + " 个接口";
        }
        infoLabel = new JLabel(ToolkitIcons.TAG);
        infoLabel.setText(" " + interfaceInfo);
        infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD, 13));
        panel.add(infoLabel, BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new BorderLayout(4, 0));
        searchLabel = new JBLabel("搜索:");
        searchLabel.setLabelFor(searchField);
        searchField = new JBTextField(12);
        searchField.putClientProperty("JComponent.placeholderText", "过滤标签\u2026");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { filterTags(); }
            @Override
            public void removeUpdate(DocumentEvent e) { filterTags(); }
            @Override
            public void changedUpdate(DocumentEvent e) { filterTags(); }
        });
        searchPanel.add(searchLabel, BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        panel.add(searchPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createListPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));

        listModel = new DefaultListModel<>();
        tagList = new JBList<>(listModel);
        tagList.setCellRenderer(new TagItemRenderer());
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setFixedCellHeight(36);
        tagList.setVisibleRowCount(10);

        tagList.addListSelectionListener(e -> {
            TagItem selected = tagList.getSelectedValue();
            if (selected != null && selected.separator) {
                tagList.clearSelection();
            }
        });

        tagList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int index = tagList.locationToIndex(e.getPoint());
                if (index < 0) return;
                TagItem item = listModel.getElementAt(index);
                if (item.separator) {
                    tagList.clearSelection();
                    return;
                }

                if (e.getClickCount() == 2) {
                    filterByTag(item);
                }
            }
        });

        tagList.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE) {
                    TagItem selected = tagList.getSelectedValue();
                    if (selected != null && !selected.separator) {
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
        addField.putClientProperty("JComponent.placeholderText", "输入新标签名(支持 name:value 格式)，回车添加\u2026");
        addField.addActionListener(e -> addNewTag());

        JButton addButton = new JButton("添加");
        addButton.addActionListener(e -> addNewTag());

        // 当没有选中接口时，禁用添加功能
        if (items.isEmpty()) {
            addField.setEnabled(false);
            addField.putClientProperty("JComponent.placeholderText", "请先在树中选中接口后再添加标签");
            addButton.setEnabled(false);
        }

        panel.add(addField, BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.EAST);

        return panel;
    }

    /**
     * 解析标签输入，支持 "tagName" 或 "tagName:tagValue" 格式
     */
    private String[] parseTagInput(String input) {
        int colonIndex = input.indexOf(':');
        if (colonIndex > 0 && colonIndex < input.length() - 1) {
            return new String[]{input.substring(0, colonIndex).trim(), input.substring(colonIndex + 1).trim()};
        }
        return new String[]{input.trim(), null};
    }

    private void loadTags() {
        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        // 计算每个标签在选中接口上的应用状态
        // appliedCount: 标签应用到了多少个选中接口
        Map<String, Integer> tagAppliedCount = new HashMap<>();
        for (InterfaceItem item : items) {
            List<TagEntity> currentTags = adapter.loadTagsByInterface(
                    projectPath,
                    item.getModule() != null ? item.getModule().getName() : "",
                    item.getInterfaceItemCategoryEnum().name(),
                    item.getUrl(),
                    item.getMethod() != null ? item.getMethod().name() : null,
                    item.getPsiMethod() != null ? item.getPsiMethod().getName() : ""
            );
            for (TagEntity tag : currentTags) {
                tagAppliedCount.merge(tag.getTagName(), 1, Integer::sum);
            }
        }

        // 收集去重标签及其 sortOrder（取最小值）
        Map<String, Integer> tagSortOrderMap = new LinkedHashMap<>();
        Map<String, Integer> tagCountMap = new LinkedHashMap<>();
        for (TagEntity tag : adapter.loadTags(projectPath)) {
            String name = tag.getTagName();
            tagCountMap.put(name, tagCountMap.getOrDefault(name, 0) + 1);
            int order = tag.getSortOrder() != null ? tag.getSortOrder() : 0;
            tagSortOrderMap.merge(name, order, Integer::min);
        }

        // 如果所有标签的 sortOrder 都是 0，按字母顺序初始化
        boolean allZero = tagSortOrderMap.values().stream().allMatch(v -> v == 0);
        if (allZero && !tagSortOrderMap.isEmpty()) {
            List<String> sortedNames = new ArrayList<>(tagSortOrderMap.keySet());
            Collections.sort(sortedNames);
            for (int i = 0; i < sortedNames.size(); i++) {
                tagSortOrderMap.put(sortedNames.get(i), (i + 1) * 10);
                adapter.updateTagSortOrder(projectPath, sortedNames.get(i), (i + 1) * 10);
            }
        }

        List<String> allTagNames = new ArrayList<>(tagSortOrderMap.keySet());
        allTagNames.sort((a, b) -> {
            int orderA = tagSortOrderMap.getOrDefault(a, 0);
            int orderB = tagSortOrderMap.getOrDefault(b, 0);
            return Integer.compare(orderA, orderB);
        });

        List<TagItem> itemList = new ArrayList<>();
        for (String tagName : allTagNames) {
            int count = tagCountMap.getOrDefault(tagName, 0);
            int appliedCount = tagAppliedCount.getOrDefault(tagName, 0);
            boolean applied = appliedCount == items.size() && !items.isEmpty();
            boolean partialApplied = appliedCount > 0 && appliedCount < items.size();
            int sortOrder = tagSortOrderMap.getOrDefault(tagName, 0);
            itemList.add(new TagItem(tagName, count, applied, partialApplied, false, sortOrder));
        }

        allTagItems = itemList;
        applyFilter("");
    }

    private void filterTags() {
        String text = searchField.getText().trim().toLowerCase();
        applyFilter(text);
    }

    private void applyFilter(String filter) {
        listModel.clear();
        List<TagItem> applied = new ArrayList<>();
        List<TagItem> partial = new ArrayList<>();
        List<TagItem> notApplied = new ArrayList<>();

        for (TagItem item : allTagItems) {
            if (!filter.isEmpty() && !item.tagName.toLowerCase().contains(filter)) {
                continue;
            }
            if (item.applied) {
                applied.add(item);
            } else if (item.partialApplied) {
                partial.add(item);
            } else {
                notApplied.add(item);
            }
        }

        if (!applied.isEmpty()) {
            listModel.addElement(new TagItem("\u2500\u2500 \u5DF2\u6DFB\u52A0 \u2500\u2500", -1, true, false, true));
            for (TagItem item : applied) {
                listModel.addElement(item);
            }
        }

        if (!partial.isEmpty()) {
            listModel.addElement(new TagItem("\u2500\u2500 \u90E8\u5206\u6DFB\u52A0 \u2500\u2500", -1, false, true, true));
            for (TagItem item : partial) {
                listModel.addElement(item);
            }
        }

        if (!notApplied.isEmpty()) {
            listModel.addElement(new TagItem("\u2500\u2500 \u53EF\u6DFB\u52A0 \u2500\u2500", -1, false, false, true));
            for (TagItem item : notApplied) {
                listModel.addElement(item);
            }
        }

        if (listModel.isEmpty()) {
            listModel.addElement(new TagItem("\u6682\u65E0\u6807\u7B7E", -1, false, false, true));
        }

        updateStatus();
    }

    private void addNewTag() {
        String input = addField.getText().trim();
        if (input.isEmpty()) return;

        String[] parsed = parseTagInput(input);
        String tagName = parsed[0];
        String tagValue = parsed[1];

        log.info("addNewTag: tagName={}, tagValue={}, items.size={}", tagName, tagValue, items.size());

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        int addedCount = 0;
        for (InterfaceItem item : items) {
            List<TagEntity> existing = adapter.loadTagsByInterface(
                    projectPath,
                    item.getModule() != null ? item.getModule().getName() : "",
                    item.getInterfaceItemCategoryEnum().name(),
                    item.getUrl(),
                    item.getMethod() != null ? item.getMethod().name() : null,
                    item.getPsiMethod() != null ? item.getPsiMethod().getName() : ""
            );

            boolean alreadyExists = existing.stream().anyMatch(t -> t.getTagName().equals(tagName));
            if (alreadyExists) {
                log.info("addNewTag: tag {} already exists for item {}", tagName, item.getName());
                continue;
            }

            TagEntity tagEntity = TagEntity.builder()
                    .projectPath(projectPath)
                    .moduleName(item.getModule() != null ? item.getModule().getName() : "")
                    .category(item.getInterfaceItemCategoryEnum().name())
                    .url(item.getUrl())
                    .httpMethod(item.getMethod() != null ? item.getMethod().name() : null)
                    .methodName(item.getPsiMethod() != null ? item.getPsiMethod().getName() : "")
                    .tagName(tagName)
                    .tagValue(tagValue)
                    .createdTime(System.currentTimeMillis())
                    .updatedTime(System.currentTimeMillis())
                    .build();

            log.info("addNewTag: saving tag {} for item {}", tagName, item.getName());
            adapter.saveTag(tagEntity);
            addedCount++;
        }

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        addField.setText("");
        String valueInfo = tagValue != null ? ":" + tagValue : "";
        if (addedCount > 0) {
            statusLabel.setText("已添加标签: " + tagName + valueInfo + " (到 " + addedCount + " 个接口)");
        } else if (items.isEmpty()) {
            statusLabel.setText("请先在树中选中接口，再添加标签");
        } else {
            statusLabel.setText("标签 \"" + tagName + "\" 已存在于当前接口");
        }
        loadTags();
    }

    private void applyTag(TagItem item) {
        if (items.isEmpty()) {
            statusLabel.setText("请先选择一个接口");
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        int appliedCount = 0;
        for (InterfaceItem currentItem : items) {
            List<TagEntity> existing = adapter.loadTagsByInterface(
                    projectPath,
                    currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                    currentItem.getInterfaceItemCategoryEnum().name(),
                    currentItem.getUrl(),
                    currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                    currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
            );

            boolean alreadyExists = existing.stream().anyMatch(t -> t.getTagName().equals(item.tagName));
            if (alreadyExists) continue;

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
            appliedCount++;
        }

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        statusLabel.setText("已应用标签: " + item.tagName + " (到 " + appliedCount + " 个接口)");
        loadTags();
    }

    private void removeTag(TagItem item) {
        if (items.isEmpty()) return;

        int result = Messages.showYesNoDialog(project,
                "确定要移除标签 \u201C" + item.tagName + "\u201D 吗？",
                "移除标签",
                Messages.getQuestionIcon());
        if (result != Messages.YES) return;

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        int removedCount = 0;
        for (InterfaceItem currentItem : items) {
            List<TagEntity> existing = adapter.loadTagsByInterface(
                    projectPath,
                    currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                    currentItem.getInterfaceItemCategoryEnum().name(),
                    currentItem.getUrl(),
                    currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                    currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
            );

            boolean hasTag = existing.stream().anyMatch(t -> t.getTagName().equals(item.tagName));
            if (!hasTag) continue;

            adapter.deleteTag(
                    projectPath,
                    currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                    currentItem.getInterfaceItemCategoryEnum().name(),
                    currentItem.getUrl(),
                    currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                    currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : "",
                    item.tagName
            );
            removedCount++;
        }

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        statusLabel.setText("已移除标签: " + item.tagName + " (从 " + removedCount + " 个接口)");
        loadTags();
    }

    private void deleteTag(TagItem item) {
        int choice = Messages.showYesNoDialog(project,
                "确定要删除标签 \u201C" + item.tagName + "\u201D 吗？\n这将删除所有接口上的此标签。",
                "确认删除",
                Messages.getQuestionIcon());
        if (choice != Messages.YES) return;

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
            navigator.toggleTagFilter(item.tagName);
            close(OK_EXIT_CODE);
        }
    }

    private void moveTagUp() {
        TagItem selected = tagList.getSelectedValue();
        if (selected == null || selected.separator) return;

        // 找到当前选中项在列表中的位置（跳过 separator）
        int selectedIndex = -1;
        TagItem prevTag = null;
        for (int i = 0; i < listModel.size(); i++) {
            TagItem item = listModel.getElementAt(i);
            if (item.separator) continue;
            if (item.tagName.equals(selected.tagName)) {
                selectedIndex = i;
                break;
            }
            prevTag = item;
        }

        if (prevTag == null) {
            statusLabel.setText("已经是第一个标签");
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        // 交换 sortOrder
        adapter.updateTagSortOrder(projectPath, selected.tagName, prevTag.sortOrder);
        adapter.updateTagSortOrder(projectPath, prevTag.tagName, selected.sortOrder);

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
        loadTags();

        // 恢复选中状态
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.getElementAt(i).tagName.equals(selected.tagName)) {
                tagList.setSelectedIndex(i);
                break;
            }
        }

        statusLabel.setText("已上移标签: " + selected.tagName);
    }

    private void moveTagDown() {
        TagItem selected = tagList.getSelectedValue();
        if (selected == null || selected.separator) return;

        // 找到当前选中项在列表中的下一个非 separator 项
        boolean foundSelected = false;
        TagItem nextTag = null;
        for (int i = 0; i < listModel.size(); i++) {
            TagItem item = listModel.getElementAt(i);
            if (item.separator) continue;
            if (foundSelected) {
                nextTag = item;
                break;
            }
            if (item.tagName.equals(selected.tagName)) {
                foundSelected = true;
            }
        }

        if (nextTag == null) {
            statusLabel.setText("已经是最后一个标签");
            return;
        }

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        // 交换 sortOrder
        adapter.updateTagSortOrder(projectPath, selected.tagName, nextTag.sortOrder);
        adapter.updateTagSortOrder(projectPath, nextTag.tagName, selected.sortOrder);

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
        loadTags();

        // 恢复选中状态
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.getElementAt(i).tagName.equals(selected.tagName)) {
                tagList.setSelectedIndex(i);
                break;
            }
        }

        statusLabel.setText("已下移标签: " + selected.tagName);
    }

    private void updateStatus() {
        long appliedCount = allTagItems.stream().filter(i -> i.applied).count();
        long partialCount = allTagItems.stream().filter(i -> i.partialApplied).count();
        long totalCount = allTagItems.size();
        String info = "共 " + totalCount + " 个标签";
        if (items.size() > 1) {
            info += "，已全部添加 " + appliedCount + " 个，部分添加 " + partialCount + " 个";
        } else if (!items.isEmpty()) {
            info += "，当前接口已添加 " + appliedCount + " 个";
        }
        statusLabel.setText(info);
    }

    public void showDialog() {
        show();
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + "\u2026";
    }

    private static class TagItem {
        final String tagName;
        final int usageCount;
        final boolean applied;
        final boolean partialApplied;
        final boolean separator;
        final int sortOrder;

        TagItem(String tagName, int usageCount, boolean applied) {
            this(tagName, usageCount, applied, false, false, 0);
        }

        TagItem(String tagName, int usageCount, boolean applied, boolean partialApplied) {
            this(tagName, usageCount, applied, partialApplied, false, 0);
        }

        TagItem(String tagName, int usageCount, boolean applied, boolean partialApplied, boolean separator) {
            this(tagName, usageCount, applied, partialApplied, separator, 0);
        }

        TagItem(String tagName, int usageCount, boolean applied, boolean partialApplied, boolean separator, int sortOrder) {
            this.tagName = tagName;
            this.usageCount = usageCount;
            this.applied = applied;
            this.partialApplied = partialApplied;
            this.separator = separator;
            this.sortOrder = sortOrder;
        }
    }

    private static Color getTagColor(String tagName) {
        int index = Math.abs(tagName.hashCode()) % TAG_PALETTE.length;
        return TAG_PALETTE[index];
    }

    private class TagItemRenderer extends ColoredListCellRenderer<TagItem> {

        private static final Color GREEN = new JBColor(new Color(0x43, 0xA0, 0x47), new Color(0x66, 0xBB, 0x6A));
        private static final Color RED = new JBColor(new Color(0xE5, 0x39, 0x35), new Color(0xEF, 0x53, 0x50));
        private static final Color ORANGE = new JBColor(new Color(0xFB, 0x8C, 0x00), new Color(0xFF, 0xA7, 0x26));
        private static final Color GRAY = JBColor.GRAY;

        @Override
        protected void customizeCellRenderer(@NotNull JList<? extends TagItem> list,
                                              TagItem value, int index,
                                              boolean selected, boolean hasFocus) {

            if (value.separator) {
                append(value.tagName, new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_SMALLER, GRAY));
                setBorder(JBUI.Borders.empty(4, 8, 4, 8));
                return;
            }

            setBorder(JBUI.Borders.empty(4, 8));

            // 标签名 - 使用标签对应颜色
            Color tagColor = getTagColor(value.tagName);
            append("\u25CF ", new com.intellij.ui.SimpleTextAttributes(
                    com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, tagColor));
            append(truncate(value.tagName, MAX_TAG_DISPLAY_LEN), new com.intellij.ui.SimpleTextAttributes(
                    com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, tagColor));

            if (value.tagName.length() > MAX_TAG_DISPLAY_LEN) {
                setToolTipText(value.tagName);
            }

            // 使用次数 - 灰色
            if (value.usageCount >= 0) {
                append(" (" + value.usageCount + ")", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, GRAY));
            }

            // 状态标记 - 统一配色
            if (value.applied) {
                append(" \u2713", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_BOLD, GREEN));
                append(" [移除]", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, RED));
            } else if (value.partialApplied) {
                append(" \u25D0", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_BOLD, ORANGE));
                append(" [补全]", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, ORANGE));
            } else {
                append(" [添加]", new com.intellij.ui.SimpleTextAttributes(
                        com.intellij.ui.SimpleTextAttributes.STYLE_PLAIN, GREEN));
            }

            setToolTipText("双击过滤 | Delete 删除标签");
        }
    }

    @Override
    protected JComponent createSouthPanel() {
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.setBorder(JBUI.Borders.empty(8, 0, 0, 0));

        JPanel actionPanel = new JPanel();
        actionPanel.setLayout(new BoxLayout(actionPanel, BoxLayout.X_AXIS));
        actionPanel.add(Box.createHorizontalGlue());

        JButton applyOrRemoveBtn = new JButton();
        applyOrRemoveBtn.setText("应用/移除");
        applyOrRemoveBtn.setToolTipText("应用或移除选中标签");
        applyOrRemoveBtn.addActionListener(e -> {
            TagItem selected = tagList.getSelectedValue();
            if (selected == null || selected.separator) {
                statusLabel.setText("请先选择一个标签");
                return;
            }
            if (selected.applied) {
                removeTag(selected);
            } else {
                applyTag(selected);
            }
        });
        actionPanel.add(applyOrRemoveBtn);
        actionPanel.add(Box.createHorizontalStrut(6));

        JButton deleteBtn = new JButton("删除标签");
        deleteBtn.setToolTipText("永久删除此标签（所有接口）");
        deleteBtn.addActionListener(e -> {
            TagItem selected = tagList.getSelectedValue();
            if (selected == null || selected.separator) {
                statusLabel.setText("请先选择一个标签");
                return;
            }
            deleteTag(selected);
        });
        actionPanel.add(deleteBtn);
        actionPanel.add(Box.createHorizontalStrut(6));

        JButton moveUpBtn = new JButton("\u2191");
        moveUpBtn.setToolTipText("上移标签");
        moveUpBtn.setMargin(new Insets(2, 6, 2, 6));
        moveUpBtn.addActionListener(e -> moveTagUp());
        actionPanel.add(moveUpBtn);

        JButton moveDownBtn = new JButton("\u2193");
        moveDownBtn.setToolTipText("下移标签");
        moveDownBtn.setMargin(new Insets(2, 6, 2, 6));
        moveDownBtn.addActionListener(e -> moveTagDown());
        actionPanel.add(moveDownBtn);
        actionPanel.add(Box.createHorizontalStrut(6));

        JButton cancelBtn = new JButton(getCancelAction());
        actionPanel.add(cancelBtn);

        southPanel.add(actionPanel, BorderLayout.EAST);

        return southPanel;
    }
}
