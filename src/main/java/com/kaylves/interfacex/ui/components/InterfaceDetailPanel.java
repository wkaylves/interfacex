package com.kaylves.interfacex.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.SimpleNode;
import com.intellij.ui.treeStructure.SimpleTree;
import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.db.model.TagEntity;
import com.kaylves.interfacex.db.storage.StorageAdapter;
import com.kaylves.interfacex.service.InterfaceXNavigator;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class InterfaceDetailPanel extends JPanel implements TreeSelectionListener {

    private final Project project;
    private final SimpleTree simpleTree;

    private JLabel urlLabel;
    private JLabel methodLabel;
    private JLabel classNameLabel;
    private JLabel methodNameLabel;
    private JLabel selectionCountLabel;
    private TagQuickSelectorPanel tagSelectorPanel;
    private JPanel detailPanel;
    private JPanel singleInfoPanel;
    private JPanel multiInfoPanel;
    private InterfaceItem currentItem;

    public InterfaceDetailPanel(Project project, SimpleTree simpleTree) {
        super(new BorderLayout(5, 5));
        this.project = project;
        this.simpleTree = simpleTree;

        setBorder(new EmptyBorder(10, 10, 10, 10));
        initComponents();

        simpleTree.addTreeSelectionListener(this);
    }

    private void initComponents() {
        detailPanel = createDetailPanel();

        JBScrollPane scrollPane = new JBScrollPane(detailPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createDetailPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(UIManager.getColor("Panel.background"));

        JPanel headerPanel = new JPanel(new BorderLayout(5, 5));
        headerPanel.setBorder(new EmptyBorder(0, 0, 10, 0));

        JLabel titleLabel = new JLabel("接口详情");
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        panel.add(headerPanel);

        JSeparator separator1 = new JSeparator();
        panel.add(separator1);

        singleInfoPanel = new JPanel();
        singleInfoPanel.setLayout(new BoxLayout(singleInfoPanel, BoxLayout.Y_AXIS));
        singleInfoPanel.setOpaque(false);

        JPanel urlPanel = createInfoPanel("URL:");
        urlLabel = new JLabel();
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, 13));
        urlLabel.setForeground(UIManager.getColor("Label.foreground"));
        urlLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, urlLabel.getPreferredSize().height));
        urlPanel.add(urlLabel);
        singleInfoPanel.add(urlPanel);

        singleInfoPanel.add(new JSeparator());

        JPanel methodPanel = createInfoPanel("HTTP 方法:");
        methodLabel = new JLabel();
        methodLabel.setFont(methodLabel.getFont().deriveFont(Font.BOLD, 12));
        methodPanel.add(methodLabel);
        singleInfoPanel.add(methodPanel);

        singleInfoPanel.add(new JSeparator());

        JPanel classPanel = createInfoPanel("类名:");
        classNameLabel = new JLabel();
        classNameLabel.setFont(classNameLabel.getFont().deriveFont(Font.PLAIN, 12));
        classPanel.add(classNameLabel);
        singleInfoPanel.add(classPanel);

        singleInfoPanel.add(new JSeparator());

        JPanel methodInfoPanel = createInfoPanel("方法名:");
        methodNameLabel = new JLabel();
        methodNameLabel.setFont(methodNameLabel.getFont().deriveFont(Font.PLAIN, 12));
        methodInfoPanel.add(methodNameLabel);
        singleInfoPanel.add(methodInfoPanel);

        singleInfoPanel.add(new JSeparator());

        panel.add(singleInfoPanel);

        multiInfoPanel = new JPanel(new BorderLayout(5, 5));
        multiInfoPanel.setBorder(new EmptyBorder(8, 0, 8, 0));
        multiInfoPanel.setOpaque(false);
        selectionCountLabel = new JBLabel();
        selectionCountLabel.setFont(selectionCountLabel.getFont().deriveFont(Font.PLAIN, 13));
        selectionCountLabel.setForeground(JBColor.GRAY);
        multiInfoPanel.add(selectionCountLabel, BorderLayout.WEST);
        multiInfoPanel.setVisible(false);
        panel.add(multiInfoPanel);

        JPanel tagPanel = new JPanel(new BorderLayout(5, 5));
        tagPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        tagSelectorPanel = new TagQuickSelectorPanel(project, null, this::onTagChanged);
        tagPanel.add(tagSelectorPanel, BorderLayout.CENTER);

        panel.add(tagPanel);

        panel.add(Box.createVerticalGlue());

        panel.add(Box.createVerticalStrut(10));

        return panel;
    }

    private JPanel createInfoPanel(String labelText) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(new EmptyBorder(8, 0, 8, 0));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel label = new JLabel(labelText);
        label.setFont(label.getFont().deriveFont(Font.PLAIN, 12));
        label.setForeground(UIManager.getColor("Label.foreground"));
        label.setPreferredSize(new Dimension(80, label.getPreferredSize().height));

        panel.add(label, BorderLayout.WEST);
        return panel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        List<InterfaceItem> selectedItems = collectSelectedItems();

        if (selectedItems.isEmpty()) {
            currentItem = null;
            clearDetail();
        } else if (selectedItems.size() == 1) {
            currentItem = selectedItems.get(0);
            updateSingleDetail(currentItem);
        } else {
            currentItem = null;
            updateMultiDetail(selectedItems);
        }
    }

    private List<InterfaceItem> collectSelectedItems() {
        Set<InterfaceItem> itemSet = new LinkedHashSet<>();

        try {
            SimpleNode[] selectedNodes = simpleTree.getSelectedNodes(SimpleNode.class, null);
            if (selectedNodes != null) {
                for (SimpleNode node : selectedNodes) {
                    collectServiceItemsFromNode(node, itemSet);
                }
            }
        } catch (Exception e) {
            log.debug("getSelectedNodes failed", e);
        }

        if (itemSet.isEmpty()) {
            try {
                javax.swing.tree.TreePath[] paths = simpleTree.getSelectionPaths();
                if (paths != null) {
                    for (javax.swing.tree.TreePath path : paths) {
                        Object component = path.getLastPathComponent();
                        if (component instanceof SimpleNode) {
                            collectServiceItemsFromNode((SimpleNode) component, itemSet);
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("getSelectionPaths failed", e);
            }
        }

        if (itemSet.isEmpty()) {
            SimpleNode singleNode = simpleTree.getSelectedNode();
            if (singleNode != null) {
                collectServiceItemsFromNode(singleNode, itemSet);
            }
        }

        return new ArrayList<>(itemSet);
    }

    private void collectServiceItemsFromNode(SimpleNode node, Set<InterfaceItem> result) {
        if (node instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            result.add(serviceNode.interfaceItem);
            return;
        }
        SimpleNode[] children = node.getChildren();
        if (children != null) {
            for (SimpleNode child : children) {
                collectServiceItemsFromNode(child, result);
            }
        }
    }

    private void updateSingleDetail(InterfaceItem item) {
        singleInfoPanel.setVisible(true);
        multiInfoPanel.setVisible(false);

        urlLabel.setText(item.getUrl() != null ? item.getUrl() : "-");

        if (item.getMethod() != null) {
            methodLabel.setText(item.getMethod().name());
            methodLabel.setForeground(getMethodColor(item.getMethod().name()));
        } else {
            methodLabel.setText("-");
            methodLabel.setForeground(UIManager.getColor("Label.foreground"));
        }

        classNameLabel.setText(item.getCachedClassName() != null ? item.getCachedClassName() : "-");
        methodNameLabel.setText(item.getCachedMethodName() != null ? item.getCachedMethodName() : "-");

        tagSelectorPanel.setCurrentItem(item);

        revalidate();
        repaint();
    }

    private void updateMultiDetail(List<InterfaceItem> selectedItems) {
        singleInfoPanel.setVisible(false);
        multiInfoPanel.setVisible(true);

        selectionCountLabel.setText("已选中 " + selectedItems.size() + " 个接口，右键可批量设置标签");

        tagSelectorPanel.setCurrentItem(null);

        revalidate();
        repaint();
    }

    private void clearDetail() {
        singleInfoPanel.setVisible(true);
        multiInfoPanel.setVisible(false);

        urlLabel.setText("-");
        methodLabel.setText("-");
        methodLabel.setForeground(UIManager.getColor("Label.foreground"));
        classNameLabel.setText("-");
        methodNameLabel.setText("-");
        tagSelectorPanel.setCurrentItem(null);
    }

    private Color getMethodColor(String method) {
        return switch (method.toUpperCase()) {
            case "GET" -> new Color(0, 128, 0);
            case "POST" -> new Color(0, 0, 192);
            case "PUT" -> new Color(255, 165, 0);
            case "DELETE" -> new Color(255, 0, 0);
            case "PATCH" -> new Color(128, 0, 128);
            default -> UIManager.getColor("Label.foreground");
        };
    }

    private void onTagChanged() {
        InterfaceXNavigator.getInstance(project).scheduleStructureUpdate();
    }

    public void refresh() {
        if (currentItem != null) {
            updateSingleDetail(currentItem);
        }
    }
}
