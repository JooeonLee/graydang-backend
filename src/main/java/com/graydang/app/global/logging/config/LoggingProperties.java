package com.graydang.app.global.logging.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "logging.custom")
@Configuration
@Getter
@Setter
public class LoggingProperties {

    private Boolean enabled;
    private Boolean httpRequest;
}
