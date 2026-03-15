package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.monitoring.OpenClawMetrics;
import com.openclaw.spring.properties.OpenClawProperties;
import com.openclaw.spring.websocket.OpenClawWebSocketClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Phase 6 (WebSocket + Metrics) 自动配置测试
 */
class Phase6AutoConfigurationTest {

    // ---- WebSocket 测试 ----

    @Test
    @DisplayName("WebSocket - 自动注册")
    void shouldAutoConfigureWebSocket() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        EventAutoConfiguration.class,
                        OpenClawAutoConfiguration.class,
                        WebSocketAutoConfiguration.class
                ))
                .withPropertyValues(
                        "openclaw.gateway.url=http://localhost:18789",
                        "openclaw.websocket.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenClawWebSocketClient.class);
                });
    }

    @Test
    @DisplayName("WebSocket - disabled时不注册")
    void shouldNotConfigureWebSocketWhenDisabled() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        EventAutoConfiguration.class,
                        OpenClawAutoConfiguration.class,
                        WebSocketAutoConfiguration.class
                ))
                .withPropertyValues(
                        "openclaw.gateway.url=http://localhost:18789",
                        "openclaw.websocket.enabled=false"
                )
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OpenClawWebSocketClient.class);
                });
    }

    @Test
    @DisplayName("WebSocket - 用户自定义Bean优先")
    void shouldRespectCustomWebSocketBean() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        EventAutoConfiguration.class,
                        OpenClawAutoConfiguration.class,
                        WebSocketAutoConfiguration.class
                ))
                .withPropertyValues("openclaw.gateway.url=http://localhost:18789")
                .withUserConfiguration(CustomWebSocketConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenClawWebSocketClient.class);
                    assertThat(context.getBean("customWebSocket")).isNotNull();
                });
    }

    // ---- Metrics 测试 ----

    @Test
    @DisplayName("Metrics - 自动注册")
    void shouldAutoConfigureMetrics() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class))
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenClawMetrics.class);
                });
    }

    @Test
    @DisplayName("Metrics - 无MeterRegistry时不注册")
    void shouldNotConfigureMetricsWithoutMeterRegistry() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class))
                .run(context -> {
                    assertThat(context).doesNotHaveBean(OpenClawMetrics.class);
                });
    }

    @Test
    @DisplayName("Metrics - 用户自定义Bean优先")
    void shouldRespectCustomMetricsBean() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(MetricsAutoConfiguration.class))
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .withUserConfiguration(CustomMetricsConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenClawMetrics.class);
                    assertThat(context.getBean("customMetrics")).isNotNull();
                });
    }

    // ---- 完整集成测试 ----

    @Test
    @DisplayName("完整Phase 6 - 所有Bean注册")
    void shouldConfigureAllPhase6Beans() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(
                        EventAutoConfiguration.class,
                        OpenClawAutoConfiguration.class,
                        WebSocketAutoConfiguration.class,
                        MetricsAutoConfiguration.class
                ))
                .withPropertyValues("openclaw.gateway.url=http://localhost:18789")
                .withBean(MeterRegistry.class, SimpleMeterRegistry::new)
                .run(context -> {
                    assertThat(context).hasSingleBean(OpenClawWebSocketClient.class);
                    assertThat(context).hasSingleBean(OpenClawMetrics.class);
                });
    }

    // ---- 测试配置 ----

    @Configuration
    static class CustomWebSocketConfiguration {
        @Bean("customWebSocket")
        public OpenClawWebSocketClient customWebSocket() {
            return new OpenClawWebSocketClient(
                    new OpenClawProperties(),
                    new com.openclaw.spring.event.EventListenerRegistry()
            );
        }
    }

    @Configuration
    static class CustomMetricsConfiguration {
        @Bean("customMetrics")
        public OpenClawMetrics customMetrics(MeterRegistry registry) {
            return new OpenClawMetrics(registry);
        }
    }
}
