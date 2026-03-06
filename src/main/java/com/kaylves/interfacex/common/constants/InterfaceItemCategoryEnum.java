package com.kaylves.interfacex.common.constants;

import com.kaylves.interfacex.module.mission.MissionStrategyExport;
import com.kaylves.interfacex.module.openfeign.OpenFeignStrategyExport;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQProducerExportServiceStrategy;
import com.kaylves.interfacex.module.rabbitmq.impl.RabbitMQStrategyExport;
import com.kaylves.interfacex.module.rocketmq.impl.RocketMQDeliverExportServiceStrategy;
import com.kaylves.interfacex.module.rocketmq.RocketMQListenerStrategyExport;
import com.kaylves.interfacex.module.rocketmq.impl.RocketProducerExportServiceStrategy;
import com.kaylves.interfacex.module.spring.SpringControllerStrategyExport;
import com.kaylves.interfacex.module.xxljob.XXLJobExportServiceStrategy;
import lombok.Getter;

/**
 * @author kaylves
 */

@Getter
public enum InterfaceItemCategoryEnum {

    RocketMQProducer(RocketProducerExportServiceStrategy.class),

    RocketMQDeliver(RocketMQDeliverExportServiceStrategy.class),

    RocketMQListener(RocketMQListenerStrategyExport.class),

    RabbitMQListener(RabbitMQStrategyExport.class),

    RabbitMQProducer(RabbitMQProducerExportServiceStrategy.class),

    XXLJob(XXLJobExportServiceStrategy.class),

    HTTP(SpringControllerStrategyExport.class),

    OpenFeign(OpenFeignStrategyExport.class),

    Mission(MissionStrategyExport.class);

    /**
     * export strategy class
     */
    private final Class<?> exportStrategy;

    InterfaceItemCategoryEnum(Class<?> exportStrategy) {
        this.exportStrategy = exportStrategy;
    }

    public static InterfaceItemCategoryEnum getUniqueEnum(InterfaceItemCategoryEnum interfaceItemCategoryEnum) {
        if (interfaceItemCategoryEnum == InterfaceItemCategoryEnum.RocketMQProducer || interfaceItemCategoryEnum == InterfaceItemCategoryEnum.RocketMQDeliver) {
            return RocketMQProducer;
        }

        return interfaceItemCategoryEnum;
    }

}
