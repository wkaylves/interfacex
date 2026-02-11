package com.kaylves.interfacex.ui.navigator;

import com.kaylves.interfacex.common.InterfaceXItem;
import com.kaylves.interfacex.common.constants.InterfaceXItemCategoryEnum;

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
     * @param interfaceXItem ServiceItem
     */
    void flush(InterfaceXItem interfaceXItem);

    /**
     * 获取接口类型
     * @return InterfaceXEnum
     */
    InterfaceXItemCategoryEnum getInterfaceXEnum();
}
