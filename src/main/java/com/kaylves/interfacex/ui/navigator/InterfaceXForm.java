package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.common.InterfaceItem;
import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;

import javax.swing.*;

/**
 * InterfaceXForm
 * @author kaylves
 * @since 1.1.4
 */
public interface InterfaceXForm {
    /**
     * form面板
     * @return JPanel
     */
    JPanel  getPanel();

    /**
     * flush form
     * @param interfaceItem ServiceItem
     */
    void flush(InterfaceItem interfaceItem);

    /**
     * 获取接口类型
     * @return InterfaceXEnum
     */
    InterfaceItemCategoryEnum getInterfaceXEnum();
}
