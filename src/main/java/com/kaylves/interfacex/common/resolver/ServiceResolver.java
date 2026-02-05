package com.kaylves.interfacex.common.resolver;


import com.kaylves.interfacex.ui.navigator.ServiceItem;

import java.util.List;

public interface ServiceResolver extends com.kaylves.interfacex.service.ServiceItem {

    List<ServiceItem> findServiceItemsInModule();
}
