package com.kaylves.interfacex.module.resolver;


import com.kaylves.interfacex.common.ServiceItemCategory;
import com.kaylves.interfacex.common.InterfaceXItem;

import java.util.List;

public interface ServiceResolver extends ServiceItemCategory {

    List<InterfaceXItem> findServiceItemsInModule();
}
