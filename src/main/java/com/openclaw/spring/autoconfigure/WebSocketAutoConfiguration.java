package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.properties.OpenClawProperties;
import com.openclaw.spring.websocket.OpenClawWebSocketClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * WebSocket 自动配置
 *
 * 当满足以下条件时自动启用：
 * - classpath 中存在 WebSocketClient
 * - openclaw.websocket.enabled=true（默认）
 * - openclaw.gateway.url 已配置
 */
@AutoConfiguration(after = {EventAutoConfiguration.class, OpenClawAutoConfiguration.class})
@ConditionalOnClass(name = "org.springframework.web.reactive.socket.client.WebSocketClient")
public class WebSocketAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "openclaw.websocket", name = "enabled", havingValue = "true", matchIfMissing = true)
    public OpenClawWebSocketClient openClawWebSocketClient(
            OpenClawProperties properties,
            EventListenerRegistry eventListenerRegistry) {
        return new OpenClawWebSocketClient(properties, eventListenerRegistry);
    }
}
