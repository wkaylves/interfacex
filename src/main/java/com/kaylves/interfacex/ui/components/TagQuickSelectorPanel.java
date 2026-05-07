package com.kaylves.interfacex.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class TagQuickSelectorPanel extends JPanel {

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
    private InterfaceItem currentItem;
    private final Runnable onTagChanged;

    private JPanel tagsPanel;
    private JLabel noTagsLabel;
    private JComboBox<String> addComboBox;
    private JPanel addPanel;

    public TagQuickSelectorPanel(Project project, InterfaceItem currentItem, Runnable onTagChanged) {
        super(new BorderLayout(8, 0));
        this.project = project;
        this.currentItem = currentItem;
        this.onTagChanged = onTagChanged;

        setBorder(new EmptyBorder(4, 0, 4, 0));
        setOpaque(false);
        initComponents();
        loadTags();
    }

    public void setCurrentItem(InterfaceItem item) {
        this.currentItem = item;
        loadTags();
    }

    private void initComponents() {
        JBLabel titleLabel = new JBLabel("标签:");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD));
        titleLabel.setBorder(new EmptyBorder(0, 0, 0, 4));

        tagsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 2));
        tagsPanel.setOpaque(false);

        noTagsLabel = new JBLabel("暂无标签");
        noTagsLabel.setForeground(JBColor.GRAY);
        noTagsLabel.setFont(noTagsLabel.getFont().deriveFont(Font.PLAIN, 11));

        addComboBox = new JComboBox<>();
        addComboBox.setEditable(true);
        addComboBox.setPreferredSize(new Dimension(120, 28));
        addComboBox.setFont(addComboBox.getFont().deriveFont(Font.PLAIN, 11));
        addComboBox.putClientProperty("JComponent.placeholderText", "输入标签名...");
        addComboBox.setMaximumRowCount(8);
        addComboBox.addActionListener(e -> {
            if ("comboBoxChanged".equals(e.getActionCommand())) {
                Object selected = addComboBox.getSelectedItem();
                if (selected != null && !selected.toString().trim().isEmpty()) {
                    addTagToCurrent(selected.toString().trim());
                    addComboBox.setSelectedItem(null);
                }
            }
        });
        addComboBox.getEditor().getEditorComponent().addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    String text = addComboBox.getEditor().getItem().toString().trim();
                    if (!text.isEmpty()) {
                        addTagToCurrent(text);
                        addComboBox.getEditor().setItem("");
                        addComboBox.hidePopup();
                    }
                }
            }
        });

        addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addPanel.setOpaque(false);
        addPanel.add(addComboBox);

        JBScrollPane scrollPane = new JBScrollPane(tagsPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(JBScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JBScrollPane.VERTICAL_SCROLLBAR_NEVER);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        JPanel centerPanel = new JPanel(new BorderLayout(4, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        centerPanel.add(addPanel, BorderLayout.EAST);

        add(titleLabel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
    }

    public void loadTags() {
        tagsPanel.removeAll();
        refreshAddComboBox();

        if (currentItem == null) {
            noTagsLabel.setText("请先选择接口");
            tagsPanel.add(noTagsLabel);
            addPanel.setVisible(false);
            revalidate();
            repaint();
            return;
        }

        addPanel.setVisible(true);

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
            noTagsLabel.setText("暂无标签");
            tagsPanel.add(noTagsLabel);
        } else {
            for (TagEntity tag : tags) {
                TagChip chip = new TagChip(tag);
                tagsPanel.add(chip);
            }
        }

        revalidate();
        repaint();
    }

    private void refreshAddComboBox() {
        Object prev = addComboBox.getSelectedItem();
        addComboBox.removeAllItems();

        if (currentItem == null) return;

        StorageAdapter adapter = StorageAdapter.getInstance();
        String projectPath = project.getBasePath();

        List<TagEntity> currentTags = adapter.loadTagsByInterface(
                projectPath,
                currentItem.getModule() != null ? currentItem.getModule().getName() : "",
                currentItem.getInterfaceItemCategoryEnum().name(),
                currentItem.getUrl(),
                currentItem.getMethod() != null ? currentItem.getMethod().name() : null,
                currentItem.getPsiMethod() != null ? currentItem.getPsiMethod().getName() : ""
        );
        List<String> currentTagNames = currentTags.stream()
                .map(TagEntity::getTagName)
                .collect(Collectors.toList());

        List<String> allTagNames = adapter.loadTags(projectPath).stream()
                .map(TagEntity::getTagName)
                .distinct()
                .filter(name -> !currentTagNames.contains(name))
                .sorted()
                .collect(Collectors.toList());

        for (String name : allTagNames) {
            addComboBox.addItem(name);
        }

        if (prev != null) {
            addComboBox.setSelectedItem(prev);
        }
    }

    private void addTagToCurrent(String tagName) {
        if (currentItem == null || tagName.isEmpty()) return;

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
        if (alreadyExists) return;

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

        loadTags();

        if (onTagChanged != null) {
            onTagChanged.run();
        }
    }

    private void removeTagFromCurrent(TagEntity tag) {
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

        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();

        loadTags();

        if (onTagChanged != null) {
            onTagChanged.run();
        }
    }

    private class TagChip extends JPanel {
        private static final int ARC = 10;
        private final TagEntity tag;
        private final Color bgColor;
        private final Color fgColor;
        private boolean hovered = false;

        TagChip(TagEntity tag) {
            this.tag = tag;
            this.bgColor = getTagColor(tag.getTagName());
            this.fgColor = JBColor.WHITE;

            setLayout(new BorderLayout(2, 0));
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JLabel nameLabel = new JLabel(tag.getTagName());
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 11));
            nameLabel.setForeground(fgColor);
            nameLabel.setBorder(new EmptyBorder(0, 2, 0, 0));
            add(nameLabel, BorderLayout.CENTER);

            JLabel closeBtn = new JLabel(" ×");
            closeBtn.setFont(closeBtn.getFont().deriveFont(Font.PLAIN, 12));
            closeBtn.setForeground(fgColor);
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.setToolTipText("移除标签");
            closeBtn.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    removeTagFromCurrent(tag);
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    closeBtn.setForeground(JBColor.RED);
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    closeBtn.setForeground(fgColor);
                }
            });
            add(closeBtn, BorderLayout.EAST);

            String tooltip = tag.getTagName();
            if (tag.getTagValue() != null && !tag.getTagValue().isEmpty()) {
                tooltip += " = " + tag.getTagValue();
            }
            setToolTipText(tooltip);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hovered = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color fill = hovered ? bgColor.brighter() : bgColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension ps = super.getPreferredSize();
            return new Dimension(ps.width + 10, Math.max(ps.height, 22));
        }
    }

    private static Color getTagColor(String tagName) {
        int index = Math.abs(tagName.hashCode()) % TAG_PALETTE.length;
        return TAG_PALETTE[index];
    }
}
