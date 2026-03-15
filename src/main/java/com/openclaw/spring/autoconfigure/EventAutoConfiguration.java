package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.event.EventListenerRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(before = SkillAutoConfiguration.class)
@ConditionalOnClass(EventListenerRegistry.class)
public class EventAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public EventListenerRegistry eventListenerRegistry() {
        return new EventListenerRegistry();
    }
}
