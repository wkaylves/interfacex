package com.kaylves.interfacex.module.resolver;


import com.kaylves.interfacex.common.ServiceItemCategoryI;
import com.kaylves.interfacex.common.InterfaceItem;

import java.util.List;

/**
 * @author kaylves
 */
public interface IServiceResolver extends ServiceItemCategoryI {

    List<InterfaceItem> findServiceItemsInModule();
}
