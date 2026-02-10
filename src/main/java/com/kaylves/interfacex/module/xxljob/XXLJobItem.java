package com.kaylves.interfacex.module.xxljob;

import com.kaylves.interfacex.common.InterfaceXUrl;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XXLJobItem implements InterfaceXUrl {

    private String jobName;

    @Override
    public String getUrl() {
        return this.jobName;
    }
}
