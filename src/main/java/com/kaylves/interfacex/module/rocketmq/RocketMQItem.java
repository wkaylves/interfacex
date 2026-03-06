package com.kaylves.interfacex.module.rocketmq;

import com.kaylves.interfacex.common.InterfaceUrl;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author kaylves
 */
@Getter
@Setter
@Builder
public class RocketMQItem implements InterfaceUrl {

    private String topic;

    private String tag;

    private String keys;

    @Override
    public String getUrl() {
        return this.tag;
    }
}
