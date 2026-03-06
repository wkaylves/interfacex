package com.kaylves.interfacex.common;

/**
 * @author kaylves
 */
@FunctionalInterface
public interface InterfaceUrl {

    /**
     * 获取接口URL
     * 例如
     * HTTP为path
     * ROCKET 为tag
     * rabbitmq 为queue
     * @return String
     */
    String getUrl();
}
