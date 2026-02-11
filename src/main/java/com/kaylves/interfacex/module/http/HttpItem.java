package com.kaylves.interfacex.module.http;

import com.kaylves.interfacex.common.InterfaceXUrl;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class HttpItem implements InterfaceXUrl {

    private String url;

    @Override
    public String getUrl() {
        return this.url;
    }
}
