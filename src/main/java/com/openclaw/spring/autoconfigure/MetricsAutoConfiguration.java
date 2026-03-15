package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.monitoring.OpenClawMetrics;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * Metrics 自动配置
 *
 * 当满足以下条件时自动启用：
 * - classpath 中存在 Micrometer MeterRegistry
 * - 用户未自定义 OpenClawMetrics Bean
 */
@AutoConfiguration
@ConditionalOnClass(MeterRegistry.class)
public class MetricsAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public OpenClawMetrics openClawMetrics(MeterRegistry registry) {
        return new OpenClawMetrics(registry);
    }
}
