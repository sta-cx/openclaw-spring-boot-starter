package com.openclaw.spring.event;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class EventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventListenerRegistry eventListenerRegistry() {
        return new EventListenerRegistry();
    }
}
