package com.kaylves.interfacex.common.resolver;


import com.kaylves.interfacex.ui.navigator.RestServiceItem;
import com.kaylves.interfacex.service.ServiceItem;

import java.util.List;

public interface ServiceResolver extends ServiceItem {

    List<RestServiceItem> findServiceItemsInModule();
}
