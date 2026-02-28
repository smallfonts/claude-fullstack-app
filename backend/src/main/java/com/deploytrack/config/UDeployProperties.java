package com.deploytrack.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "udeploy")
@Data
public class UDeployProperties {
    private String baseUrl;
    private String authToken;
    private boolean sslVerify = true;
}
