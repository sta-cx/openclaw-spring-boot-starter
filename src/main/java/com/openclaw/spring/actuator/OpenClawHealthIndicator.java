package com.openclaw.spring.actuator;

import com.openclaw.spring.client.OpenClawClient;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

import java.util.Map;

/**
 * OpenClaw Gateway 健康检查
 * 
 * 通过调用 Gateway 的 /api/status 接口检查连接状态。
 * 
 * 使用方式：
 * <pre>
 * management:
 *   endpoints:
 *     web:
 *       exposure:
 *         include: health
 * </pre>
 * 
 * Health endpoint 返回：
 * - UP: Gateway 连接正常
 * - DOWN: Gateway 不可达或返回错误
 */
public class OpenClawHealthIndicator implements HealthIndicator {

    private final OpenClawClient client;

    public OpenClawHealthIndicator(OpenClawClient client) {
        this.client = client;
    }

    @Override
    public Health health() {
        try {
            Map status = client.getStatus()
                    .block(java.time.Duration.ofSeconds(5));

            if (status != null) {
                return Health.up()
                        .withDetail("gateway", "connected")
                        .withDetail("status", status)
                        .build();
            } else {
                return Health.down()
                        .withDetail("gateway", "no response")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("gateway", "unreachable")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
