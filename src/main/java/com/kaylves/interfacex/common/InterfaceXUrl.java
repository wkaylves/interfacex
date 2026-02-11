package com.kaylves.interfacex.common;

@FunctionalInterface
public interface InterfaceXUrl {

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
