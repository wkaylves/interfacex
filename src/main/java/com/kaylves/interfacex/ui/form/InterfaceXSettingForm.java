package com.kaylves.interfacex.ui.form;

import com.kaylves.interfacex.db.storage.StorageType;
import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Getter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class InterfaceXSettingForm {
    private JCheckBox HTTPCheckBox;
    private JCheckBox rocketMQProducerCheckBox;
    private JCheckBox rabbitMQListenerCheckBox;
    private JCheckBox rabbitMQProducerCheckBox;
    private JCheckBox missionCheckBox;
    private JCheckBox rocketMQLisntenerCheckBox;
    private JCheckBox openFeignCheckBox;
    private JButton saveBtn;
    private JButton cancelBtn;
    @Getter
    private JPanel rootPanel;

    private JCheckBox XXLJOBCheckBox;
    private JCheckBox rabbitMQTemplateCheckBox;
    private JCheckBox rocketMQTemplateCheckBox;
    private JComboBox<StorageType> storageTypeComboBox;

    public InterfaceXSettingForm() {
        storageTypeComboBox.setModel(new DefaultComboBoxModel<>(StorageType.values()));
        storageTypeComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public JLabel getListCellRendererComponent(JList<?> list, Object value, int index,
                                                       boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof StorageType) {
                    StorageType type = (StorageType) value;
                    switch (type) {
                        case SQLITE:
                            setText("SQLite (推荐)");
                            break;
                        case XML:
                            setText("XML");
                            break;
                        default:
                            setText(type.name());
                    }
                }
                return this;
            }
        });
    }

    public void setUI(List<InterfaceItemConfigEntity> interfaceItemConfigEntities) {
        if (interfaceItemConfigEntities == null) {
            return;
        }

        interfaceItemConfigEntities.forEach(entity -> {
            if (HTTPCheckBox.getText().equals(entity.getItemCategory())) {
                HTTPCheckBox.setSelected(true);
            }
            if (rocketMQProducerCheckBox.getText().equals(entity.getItemCategory())) {
                rocketMQProducerCheckBox.setSelected(true);
            }
            if (rabbitMQListenerCheckBox.getText().equals(entity.getItemCategory())) {
                rabbitMQListenerCheckBox.setSelected(true);
            }
            if (rabbitMQProducerCheckBox.getText().equals(entity.getItemCategory())) {
                rabbitMQProducerCheckBox.setSelected(true);
            }
            if (rocketMQLisntenerCheckBox.getText().equals(entity.getItemCategory())) {
                rocketMQLisntenerCheckBox.setSelected(true);
            }
            if (openFeignCheckBox.getText().equals(entity.getItemCategory())) {
                openFeignCheckBox.setSelected(true);
            }
            if (missionCheckBox.getText().equals(entity.getItemCategory())) {
                missionCheckBox.setSelected(true);
            }
            if (XXLJOBCheckBox.getText().equals(entity.getItemCategory())) {
                XXLJOBCheckBox.setSelected(true);
            }
            if (rabbitMQTemplateCheckBox.getText().equals(entity.getItemCategory())) {
                rabbitMQTemplateCheckBox.setSelected(true);
            }
            if (rocketMQTemplateCheckBox.getText().equals(entity.getItemCategory())) {
                rocketMQTemplateCheckBox.setSelected(true);
            }
        });
    }

    public void setStorageType(StorageType type) {
        storageTypeComboBox.setSelectedItem(type);
    }

    public StorageType getStorageType() {
        return (StorageType) storageTypeComboBox.getSelectedItem();
    }

    public List<InterfaceItemConfigEntity> getInterfaceItemConfigEntities() {
        List<InterfaceItemConfigEntity> entityList = new ArrayList<>();

        if (HTTPCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(HTTPCheckBox.getText()));
        }
        if (rocketMQProducerCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rocketMQProducerCheckBox.getText()));
        }
        if (rabbitMQListenerCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rabbitMQListenerCheckBox.getText()));
        }
        if (rabbitMQProducerCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rabbitMQProducerCheckBox.getText()));
        }
        if (rocketMQLisntenerCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rocketMQLisntenerCheckBox.getText()));
        }
        if (openFeignCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(openFeignCheckBox.getText()));
        }
        if (missionCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(missionCheckBox.getText()));
        }
        if (XXLJOBCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(XXLJOBCheckBox.getText()));
        }
        if (rabbitMQTemplateCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rabbitMQTemplateCheckBox.getText()));
        }
        if (rocketMQTemplateCheckBox.isSelected()) {
            entityList.add(createEntityForCategory(rocketMQTemplateCheckBox.getText()));
        }

        return entityList;
    }

    private InterfaceItemConfigEntity createEntityForCategory(String category) {
        InterfaceItemConfigEntity entity = new InterfaceItemConfigEntity();
        entity.setItemCategory(category);
        entity.setEnabled(true);
        return entity;
    }
}
