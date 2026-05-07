package com.kaylves.interfacex.module.resolver;


import com.kaylves.interfacex.common.InterfaceItem;

import java.util.List;

public interface IServiceResolver {

    List<InterfaceItem> findServiceItemsInModule();

    String getServiceItemCategory();
}
