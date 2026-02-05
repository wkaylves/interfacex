package com.kaylves.interfacex.ui.navigator;

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
     * @param serviceItem ServiceItem
     */
    void flush(ServiceItem serviceItem);
}
