package com.kaylves.interfacex.module.intfitem;


import com.kaylves.interfacex.module.navigator.RestServiceItem;
import com.kaylves.interfacex.common.ServiceItem;

import java.util.List;

/**
 * 接口实现基类
 */
public interface ServiceResolver extends ServiceItem {

    /**
     * 返回module所有接口项
     * @return List
     */
    List<RestServiceItem> findServiceItemsInModule();
}
