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

    private static final int MAX_TAG_DISPLAY_LEN = 16;

    private final Project project;
    private InterfaceItem currentItem;
    private final Runnable onTagChanged;

    private JPanel tagsPanel;
    private JLabel noTagsLabel;
    private JTextField addField;
    private JPopupMenu addPopup;
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
        titleLabel.setLabelFor(addField);

        tagsPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 4, 2));
        tagsPanel.setOpaque(false);

        noTagsLabel = new JBLabel("暂无标签");
        noTagsLabel.setForeground(JBColor.GRAY);
        noTagsLabel.setFont(noTagsLabel.getFont().deriveFont(Font.PLAIN, 11));

        addField = new JTextField(10);
        addField.setFont(addField.getFont().deriveFont(Font.PLAIN, 11));
        addField.putClientProperty("JComponent.placeholderText", "添加标签(支持 name:value)\u2026");
        addField.addActionListener(e -> {
            String text = addField.getText().trim();
            if (!text.isEmpty()) {
                addTagToCurrent(text);
                addField.setText("");
                hideAddPopup();
            }
        });
        addField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                showAddPopup();
            }

            @Override
            public void focusLost(FocusEvent e) {
                Timer t = new Timer(200, ev -> hideAddPopup());
                t.setRepeats(false);
                t.start();
            }
        });
        addField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) { refreshAddPopup(); }
            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) { refreshAddPopup(); }
            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) { refreshAddPopup(); }
        });

        addPopup = new JPopupMenu();
        addPopup.setFocusable(false);

        addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        addPanel.setOpaque(false);
        addPanel.add(addField);

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

    private void showAddPopup() {
        if (!addPanel.isVisible() || currentItem == null) return;
        refreshAddPopup();
        if (addPopup.getComponentCount() > 0) {
            addPopup.show(addField, 0, addField.getHeight());
        }
    }

    private void hideAddPopup() {
        addPopup.setVisible(false);
    }

    private void refreshAddPopup() {
        if (currentItem == null) return;

        addPopup.removeAll();

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

        String filter = addField.getText().trim().toLowerCase();

        List<String> suggestions = adapter.loadTags(projectPath).stream()
                .map(TagEntity::getTagName)
                .distinct()
                .filter(name -> !currentTagNames.contains(name))
                .filter(name -> filter.isEmpty() || name.toLowerCase().contains(filter))
                .sorted()
                .collect(Collectors.toList());

        for (String name : suggestions) {
            JMenuItem item = new JMenuItem(name);
            item.addActionListener(e -> {
                addTagToCurrent(name);
                addField.setText("");
                hideAddPopup();
            });
            addPopup.add(item);
        }
    }

    private void addTagToCurrent(String input) {
        if (currentItem == null || input.isEmpty()) return;

        // 支持 "tagName:value" 快捷语法
        String tagName;
        String tagValue = null;
        int colonIndex = input.indexOf(':');
        if (colonIndex > 0 && colonIndex < input.length() - 1) {
            tagName = input.substring(0, colonIndex).trim();
            tagValue = input.substring(colonIndex + 1).trim();
        } else {
            tagName = input.trim();
        }

        if (tagName.isEmpty()) return;

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
                .tagValue(tagValue)
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

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 1) + "\u2026";
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

            String display = truncate(tag.getTagName(), MAX_TAG_DISPLAY_LEN);
            if (tag.getTagValue() != null && !tag.getTagValue().isEmpty()) {
                display += ":" + truncate(tag.getTagValue(), 8);
            }
            JLabel nameLabel = new JLabel(display);
            nameLabel.setFont(nameLabel.getFont().deriveFont(Font.PLAIN, 11));
            nameLabel.setForeground(fgColor);
            nameLabel.setBorder(new EmptyBorder(0, 4, 0, 0));
            add(nameLabel, BorderLayout.CENTER);

            JButton closeBtn = new JButton("\u00D7");
            closeBtn.setFont(closeBtn.getFont().deriveFont(Font.PLAIN, 11));
            closeBtn.setForeground(fgColor);
            closeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            closeBtn.setToolTipText("移除标签");
            closeBtn.getAccessibleContext().setAccessibleName("移除标签 " + tag.getTagName());
            closeBtn.setBorderPainted(false);
            closeBtn.setContentAreaFilled(false);
            closeBtn.setFocusPainted(false);
            closeBtn.setMargin(new Insets(0, 2, 0, 2));
            closeBtn.addActionListener(e -> removeTagFromCurrent(tag));
            closeBtn.addMouseListener(new MouseAdapter() {
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
            if (tag.getTagName().length() > MAX_TAG_DISPLAY_LEN) {
                tooltip = tag.getTagName();
            }
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

            Color fill = hovered ? bgColor.brighter().brighter() : bgColor;
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, ARC, ARC);

            g2.dispose();
            super.paintComponent(g);
        }

        @Override
        public Dimension getPreferredSize() {
            Dimension ps = super.getPreferredSize();
            return new Dimension(ps.width + 10, Math.max(ps.height, 24));
        }
    }

    private static Color getTagColor(String tagName) {
        int index = Math.abs(tagName.hashCode()) % TAG_PALETTE.length;
        return TAG_PALETTE[index];
    }

    private static class WrapLayout extends FlowLayout {
        WrapLayout(int align, int hgap, int vgap) {
            super(align, hgap, vgap);
        }

        @Override
        public Dimension preferredLayoutSize(Container target) {
            return layoutSize(target, true);
        }

        @Override
        public Dimension minimumLayoutSize(Container target) {
            return layoutSize(target, false);
        }

        private Dimension layoutSize(Container target, boolean preferred) {
            synchronized (target.getTreeLock()) {
                Insets insets = target.getInsets();
                int maxWidth = target.getWidth() - insets.left - insets.right;
                if (maxWidth <= 0) {
                    maxWidth = Integer.MAX_VALUE;
                }

                int x = 0, y = insets.top, rowHeight = 0;
                for (Component comp : target.getComponents()) {
                    if (!comp.isVisible()) continue;
                    Dimension d = preferred ? comp.getPreferredSize() : comp.getMinimumSize();
                    if (x + d.width > maxWidth && x > 0) {
                        x = insets.left;
                        y += rowHeight + getVgap();
                        rowHeight = 0;
                    }
                    x += d.width + getHgap();
                    rowHeight = Math.max(rowHeight, d.height);
                }
                y += rowHeight + insets.bottom;
                return new Dimension(maxWidth, y);
            }
        }
    }
}
