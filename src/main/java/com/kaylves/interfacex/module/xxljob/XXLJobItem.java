package com.kaylves.interfacex.module.xxljob;

import com.kaylves.interfacex.common.InterfaceUrl;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class XXLJobItem implements InterfaceUrl {

    private String jobName;

    @Override
    public String getUrl() {
        return this.jobName;
    }
}
