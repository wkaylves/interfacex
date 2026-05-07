package com.kaylves.interfacex.utils;

import com.kaylves.interfacex.common.constants.InterfaceItemCategoryEnum;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class CategoryParser {

    private static final Map<String, InterfaceItemCategoryEnum> CATEGORY_MAP = new HashMap<>();

    static {
        CATEGORY_MAP.put("Spring-MVC", InterfaceItemCategoryEnum.HTTP);
        CATEGORY_MAP.put("Jakarta", InterfaceItemCategoryEnum.HTTP);
        CATEGORY_MAP.put("Jaxrs", InterfaceItemCategoryEnum.HTTP);
        CATEGORY_MAP.put("RestTemplate", InterfaceItemCategoryEnum.HTTP);

        CATEGORY_MAP.put("RabbitMQ-Listener", InterfaceItemCategoryEnum.RabbitMQListener);
        CATEGORY_MAP.put("RabbitMQ-Producer", InterfaceItemCategoryEnum.RabbitMQProducer);

        CATEGORY_MAP.put("RocketMQ-Producer", InterfaceItemCategoryEnum.RocketMQProducer);
        CATEGORY_MAP.put("RocketMQTemplate-Producer", InterfaceItemCategoryEnum.RocketMQProducer);

        CATEGORY_MAP.put("RocketMQ-Deliver", InterfaceItemCategoryEnum.RocketMQDeliver);

        CATEGORY_MAP.put("RocketMqListener", InterfaceItemCategoryEnum.RocketMQListener);
        CATEGORY_MAP.put("ShardRocketMqListener", InterfaceItemCategoryEnum.RocketMQListener);

        CATEGORY_MAP.put("MissionClient", InterfaceItemCategoryEnum.Mission);
    }

    private CategoryParser() {
    }

    public static InterfaceItemCategoryEnum parse(String category) {
        if (category == null || category.isEmpty()) {
            return null;
        }

        InterfaceItemCategoryEnum mapped = CATEGORY_MAP.get(category);
        if (mapped != null) {
            return mapped;
        }

        try {
            return InterfaceItemCategoryEnum.valueOf(category);
        } catch (IllegalArgumentException e) {
            log.warn("Unknown category: {}, defaulting to HTTP", category);
            return InterfaceItemCategoryEnum.HTTP;
        }
    }
}