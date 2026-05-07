package com.kaylves.interfacex.ui.components;

import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBScrollPane;
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

@Slf4j
public class InterfaceDetailPanel extends JPanel implements TreeSelectionListener {

    private final Project project;
    private final com.intellij.ui.treeStructure.SimpleTree simpleTree;

    private JLabel urlLabel;
    private JLabel methodLabel;
    private JLabel classNameLabel;
    private JLabel methodNameLabel;
    private TagQuickSelectorPanel tagSelectorPanel;
    private JPanel detailPanel;
    private InterfaceItem currentItem;

    public InterfaceDetailPanel(Project project, com.intellij.ui.treeStructure.SimpleTree simpleTree) {
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

        JPanel urlPanel = createInfoPanel("URL:");
        urlLabel = new JLabel();
        urlLabel.setFont(urlLabel.getFont().deriveFont(Font.PLAIN, 13));
        urlLabel.setForeground(UIManager.getColor("Label.foreground"));
        urlLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, urlLabel.getPreferredSize().height));
        urlPanel.add(urlLabel);
        panel.add(urlPanel);

        JSeparator separator2 = new JSeparator();
        panel.add(separator2);

        JPanel methodPanel = createInfoPanel("HTTP 方法:");
        methodLabel = new JLabel();
        methodLabel.setFont(methodLabel.getFont().deriveFont(Font.BOLD, 12));
        methodPanel.add(methodLabel);
        panel.add(methodPanel);

        JSeparator separator3 = new JSeparator();
        panel.add(separator3);

        JPanel classPanel = createInfoPanel("类名:");
        classNameLabel = new JLabel();
        classNameLabel.setFont(classNameLabel.getFont().deriveFont(Font.PLAIN, 12));
        classPanel.add(classNameLabel);
        panel.add(classPanel);

        JSeparator separator4 = new JSeparator();
        panel.add(separator4);

        JPanel methodInfoPanel = createInfoPanel("方法名:");
        methodNameLabel = new JLabel();
        methodNameLabel.setFont(methodNameLabel.getFont().deriveFont(Font.PLAIN, 12));
        methodInfoPanel.add(methodNameLabel);
        panel.add(methodInfoPanel);

        JSeparator separator5 = new JSeparator();
        panel.add(separator5);

        JPanel tagPanel = new JPanel(new BorderLayout(5, 5));
        tagPanel.setBorder(new EmptyBorder(10, 0, 10, 0));
        tagPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        tagSelectorPanel = new TagQuickSelectorPanel(project, null, this::onTagChanged);
        tagPanel.add(tagSelectorPanel, BorderLayout.CENTER);
        
        panel.add(tagPanel);

        panel.add(Box.createVerticalGlue());
        
        // 添加底部间距
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
        Object selectedNode = simpleTree.getLastSelectedPathComponent();
        
        if (selectedNode instanceof InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {
            currentItem = serviceNode.interfaceItem;
            updateDetail(currentItem);
        } else {
            currentItem = null;
            clearDetail();
        }
    }

    private void updateDetail(InterfaceItem item) {
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
        
        tagSelectorPanel.loadTags();
        
        revalidate();
        repaint();
    }

    private void clearDetail() {
        urlLabel.setText("-");
        methodLabel.setText("-");
        methodLabel.setForeground(UIManager.getColor("Label.foreground"));
        classNameLabel.setText("-");
        methodNameLabel.setText("-");
        tagSelectorPanel.loadTags();
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
            updateDetail(currentItem);
        }
    }
}
