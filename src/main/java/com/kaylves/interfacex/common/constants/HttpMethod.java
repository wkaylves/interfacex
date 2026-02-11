package com.kaylves.interfacex.common.constants;

import java.util.HashMap;
import java.util.Map;

public enum HttpMethod {
    /**
     * HTTP接口方法开始
     */
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT,

    /**
     * MQ消费者
     */
    CONSUME,

    /**
     * MQ生产者
     */
    PRODUCE,

    /**
     * 定时任务执行
     */
    EXECUTE;

    private static final Map<String, HttpMethod> methodMap = new HashMap(8);

    static {
        for (HttpMethod httpMethod : values()) {
            methodMap.put(httpMethod.name(), httpMethod);
        }
    }

    public static HttpMethod getByRequestMethod(String method) {
        if (method == null || method.isEmpty()) {
            return null;
        }

        String[] split = method.split("\\.");

        if (split.length > 1) {
            method = split[split.length - 1].toUpperCase();
            return HttpMethod.valueOf(method);
        }

        return HttpMethod.valueOf(method.toUpperCase());
    }
}
