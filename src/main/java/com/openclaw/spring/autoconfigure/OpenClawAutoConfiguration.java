package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.client.OpenClawClient;
import com.openclaw.spring.properties.OpenClawProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(OpenClawClient.class)
@ConditionalOnProperty(prefix = "openclaw.gateway", name = "url")
@EnableConfigurationProperties(OpenClawProperties.class)
public class OpenClawAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenClawClient openClawClient(OpenClawProperties properties) {
        return new OpenClawClient(properties);
    }
}
