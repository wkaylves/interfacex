/*
 * *
 *  * @author Kaylves
 *  * @date ${date}
 *  * @description TODO
 *
 */

package com.kaylves.interfacex.ui.form;

import com.kaylves.interfacex.entity.InterfaceItemConfigEntity;
import lombok.Getter;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public void setUI(List<InterfaceItemConfigEntity> interfaceItemConfigEntities) {

        if(interfaceItemConfigEntities==null){
            return;
        }

        interfaceItemConfigEntities.forEach(interfaceItemConfigEntity -> {
            if(HTTPCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                HTTPCheckBox.setSelected(true);
            }

            if(rocketMQProducerCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                rocketMQProducerCheckBox.setSelected(true);
            }

            if(rabbitMQListenerCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                rabbitMQListenerCheckBox.setSelected(true);
            }

            if(rabbitMQProducerCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                rabbitMQProducerCheckBox.setSelected(true);
            }

            if(rocketMQLisntenerCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                rocketMQLisntenerCheckBox.setSelected(true);
            }

            if(openFeignCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                openFeignCheckBox.setSelected(true);
            }
            if(missionCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                missionCheckBox.setSelected(true);
            }

            if(XXLJOBCheckBox.getText().equals(interfaceItemConfigEntity.getItemCategory())){
                XXLJOBCheckBox.setSelected(true);
            }
        });
    }

    public List<InterfaceItemConfigEntity> getInterfaceItemConfigEntities(){
        List<InterfaceItemConfigEntity> entityList = new ArrayList<>();

        // 检查每一个复选框，如果被选中，就创建一个对应的实体并添加到列表中
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

        return entityList;
    }

    /**
     * 辅助方法，根据分类名称创建一个 InterfaceItemConfigEntity 对象。
     * 这里假设 InterfaceItemConfigEntity 有一个接受 itemCategory 的构造函数，
     * 或者有对应的 setter 方法。
     *
     * @param category 项目分类名称
     * @return 构建好的实体对象
     */
    private InterfaceItemConfigEntity createEntityForCategory(String category) {
        InterfaceItemConfigEntity entity = new InterfaceItemConfigEntity();
        entity.setItemCategory(category);
        entity.setEnabled(true);
        return entity;
    }
}
