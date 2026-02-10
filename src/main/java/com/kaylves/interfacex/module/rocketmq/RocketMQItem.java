package com.kaylves.interfacex.module.rocketmq;

import com.kaylves.interfacex.common.InterfaceXUrl;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author kaylves
 */
@Getter
@Setter
@Builder
public class RocketMQItem implements InterfaceXUrl {

    private String topic;

    private String tag;

    private String keys;

    @Override
    public String getUrl() {
        return this.tag;
    }
}
