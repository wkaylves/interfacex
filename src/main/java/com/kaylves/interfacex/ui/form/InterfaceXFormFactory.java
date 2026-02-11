package com.kaylves.interfacex.ui.form;

import com.kaylves.interfacex.ui.navigator.InterfaceXForm;
import com.kaylves.interfacex.ui.navigator.InterfaceXSimpleTreeStructure;

public class InterfaceXFormFactory {

    public static InterfaceXForm createInterfaceXForm(InterfaceXSimpleTreeStructure.ServiceNode serviceNode) {

        InterfaceXForm interfaceXForm = null;
        switch (serviceNode.myInterfaceXItem.getInterfaceXItemCategoryEnum()) {
            case XXLJob:
                interfaceXForm = new XXLJobForm(serviceNode.myInterfaceXItem);
                break;
            case RocketMQDeliver:
            case RocketMQProducer:
                interfaceXForm = new RocketMQForm(serviceNode.myInterfaceXItem);
                break;
        }

        return interfaceXForm;
    }
}
