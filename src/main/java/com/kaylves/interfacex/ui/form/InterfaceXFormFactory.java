package com.kaylves.interfacex.ui.form;

import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;

/**
 * @author kaylves
 */
public class InterfaceXFormFactory {

    /**
     *
     * @param serviceNode serviceNode
     * @return InterfaceXForm
     */
    public static InterfaceXForm createInterfaceForm(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {

        InterfaceXForm xForm = null;
        switch (serviceNode.interfaceItem.getInterfaceItemCategoryEnum()) {
            case XXLJob:
                xForm = new XXLJobForm(serviceNode.interfaceItem);
                break;
            case RocketMQDeliver:
            case RocketMQProducer:
                xForm = new RocketMQForm(serviceNode.interfaceItem);
                break;
        }

        return xForm;
    }
}
