package com.xiaobao.gateway.client.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "api")
@Data
public class ApiPropertis {
    private String registerAddress;
    private String env="dev";
    private boolean gray;
}
