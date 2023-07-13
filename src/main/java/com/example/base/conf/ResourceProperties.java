package com.example.base.conf;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "resource")
@Data
@Configuration
public class ResourceProperties {

    String urlPath;
    String localPath;
}
