package com.openclaw.spring.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenClaw 指标收集器测试
 */
class OpenClawMetricsTest {

    private MeterRegistry registry;
    private OpenClawMetrics metrics;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        metrics = new OpenClawMetrics(registry);
    }

    @Test
    @DisplayName("Skill 执行计数")
    void shouldRecordSkillExecutions() {
        metrics.recordSkillExecution("weather", "query", 100, true);
        metrics.recordSkillExecution("weather", "query", 150, true);
        metrics.recordSkillExecution("calculator", "add", 50, true);

        assertEquals(3, metrics.getTotalSkillExecutions());
        assertEquals(0, metrics.getTotalSkillErrors());

        // Verify metrics in registry
        assertNotNull(registry.find("openclaw.skill.executions").counter());
        assertEquals(3.0, registry.find("openclaw.skill.executions").counter().count());
    }

    @Test
    @DisplayName("Skill 错误计数")
    void shouldRecordSkillErrors() {
        metrics.recordSkillExecution("weather", "query", 100, true);
        metrics.recordSkillExecution("weather", "query", 200, false);
        metrics.recordSkillExecution("weather", "query", 50, false);

        assertEquals(3, metrics.getTotalSkillExecutions());
        assertEquals(2, metrics.getTotalSkillErrors());
    }

    @Test
    @DisplayName("Skill 耗时记录")
    void shouldRecordSkillDuration() {
        metrics.recordSkillExecution("weather", "query", 100, true);
        metrics.recordSkillExecution("weather", "query", 200, true);

        // Verify timer exists with correct tags
        var timer = registry.find("openclaw.skill.duration")
                .tag("skill", "weather")
                .tag("action", "query")
                .timer();
        assertNotNull(timer);
        assertEquals(2, timer.count());
    }

    @Test
    @DisplayName("事件发布计数")
    void shouldRecordEvents() {
        metrics.recordEvent("message.received");
        metrics.recordEvent("message.received");
        metrics.recordEvent("skill.executed");

        // Total events
        Counter total = registry.find("openclaw.events.published").counter();
        assertNotNull(total);
        assertEquals(3.0, total.count());

        // Events by type
        Counter msgReceived = registry.find("openclaw.events.by.type")
                .tag("type", "message.received").counter();
        assertNotNull(msgReceived);
        assertEquals(2.0, msgReceived.count());
    }

    @Test
    @DisplayName("WebSocket 连接状态")
    void shouldTrackWebSocketConnection() {
        assertFalse(metrics.isWebSocketConnected());

        metrics.recordWebSocketConnected();
        assertTrue(metrics.isWebSocketConnected());

        metrics.recordWebSocketDisconnected();
        assertFalse(metrics.isWebSocketConnected());
    }

    @Test
    @DisplayName("WebSocket 连接/断开计数")
    void shouldCountWebSocketEvents() {
        metrics.recordWebSocketConnected();
        metrics.recordWebSocketDisconnected();
        metrics.recordWebSocketConnected();

        Counter connections = registry.find("openclaw.websocket.connections").counter();
        Counter disconnections = registry.find("openclaw.websocket.disconnections").counter();

        assertNotNull(connections);
        assertNotNull(disconnections);
        assertEquals(2.0, connections.count());
        assertEquals(1.0, disconnections.count());
    }

    @Test
    @DisplayName("WebSocket 重连计数")
    void shouldCountReconnects() {
        metrics.recordWebSocketReconnect();
        metrics.recordWebSocketReconnect();
        metrics.recordWebSocketReconnect();

        Counter reconnects = registry.find("openclaw.websocket.reconnects").counter();
        assertNotNull(reconnects);
        assertEquals(3.0, reconnects.count());
    }

    @Test
    @DisplayName("会话计数")
    void shouldTrackSessions() {
        assertEquals(0, metrics.getActiveSessions());

        metrics.recordSessionCreated();
        metrics.recordSessionCreated();
        assertEquals(2, metrics.getActiveSessions());

        metrics.recordSessionClosed();
        assertEquals(1, metrics.getActiveSessions());
    }

    @Test
    @DisplayName("所有指标前缀正确")
    void shouldHaveCorrectMetricPrefixes() {
        // Trigger metric creation
        metrics.recordSkillExecution("test", "action", 10, true);
        metrics.recordEvent("test.event");
        metrics.recordWebSocketConnected();
        metrics.recordSessionCreated();

        // All metrics should start with "openclaw."
        registry.getMeters().forEach(meter -> {
            assertTrue(meter.getId().getName().startsWith("openclaw."),
                    "Metric should have 'openclaw.' prefix: " + meter.getId().getName());
        });
    }
}
