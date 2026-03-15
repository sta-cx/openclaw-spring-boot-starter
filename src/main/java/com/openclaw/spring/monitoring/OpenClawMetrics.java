package com.openclaw.spring.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * OpenClaw 指标收集器
 *
 * 基于 Micrometer 提供以下指标：
 * - Skill 执行次数/耗时/失败
 * - 事件发布次数
 * - WebSocket 连接状态
 * - 活跃会话数
 *
 * 所有指标前缀：openclaw.*
 * 在 Actuator /metrics 端点可见
 */
public class OpenClawMetrics {

    private final MeterRegistry registry;

    // Skill 指标
    private final Counter skillExecutions;
    private final Counter skillErrors;
    private final ConcurrentHashMap<String, Timer> skillTimers = new ConcurrentHashMap<>();

    // 事件指标
    private final Counter eventsPublished;
    private final ConcurrentHashMap<String, Counter> eventCounters = new ConcurrentHashMap<>();

    // WebSocket 指标
    private final AtomicInteger wsConnected = new AtomicInteger(0);
    private final Counter wsConnections;
    private final Counter wsDisconnections;
    private final Counter wsReconnects;

    // 会话指标
    private final AtomicInteger activeSessions = new AtomicInteger(0);
    private final AtomicLong totalSessions = new AtomicLong(0);

    public OpenClawMetrics(MeterRegistry registry) {
        this.registry = registry;

        // Skill counters
        this.skillExecutions = Counter.builder("openclaw.skill.executions")
                .description("Total skill execution count")
                .register(registry);

        this.skillErrors = Counter.builder("openclaw.skill.errors")
                .description("Total skill execution errors")
                .register(registry);

        // Event counter
        this.eventsPublished = Counter.builder("openclaw.events.published")
                .description("Total events published")
                .register(registry);

        // WebSocket counters
        this.wsConnections = Counter.builder("openclaw.websocket.connections")
                .description("Total WebSocket connections established")
                .register(registry);

        this.wsDisconnections = Counter.builder("openclaw.websocket.disconnections")
                .description("Total WebSocket disconnections")
                .register(registry);

        this.wsReconnects = Counter.builder("openclaw.websocket.reconnects")
                .description("Total WebSocket reconnection attempts")
                .register(registry);

        // Gauges (values that can go up and down)
        Gauge.builder("openclaw.websocket.connected", wsConnected, AtomicInteger::get)
                .description("Current WebSocket connection status (1=connected, 0=disconnected)")
                .register(registry);

        Gauge.builder("openclaw.sessions.active", activeSessions, AtomicInteger::get)
                .description("Number of active sessions")
                .register(registry);

        Gauge.builder("openclaw.sessions.total", totalSessions, AtomicLong::get)
                .description("Total sessions created since startup")
                .register(registry);
    }

    // ---- Skill 指标 ----

    public void recordSkillExecution(String skillName, String actionName, long durationMs, boolean success) {
        skillExecutions.increment();
        if (!success) {
            skillErrors.increment();
        }

        String key = skillName + "." + actionName;
        skillTimers.computeIfAbsent(key, k ->
                Timer.builder("openclaw.skill.duration")
                        .description("Skill execution duration")
                        .tag("skill", skillName)
                        .tag("action", actionName)
                        .register(registry)
        ).record(java.time.Duration.ofMillis(durationMs));
    }

    // ---- 事件指标 ----

    public void recordEvent(String eventType) {
        eventsPublished.increment();
        eventCounters.computeIfAbsent(eventType, k ->
                Counter.builder("openclaw.events.by.type")
                        .description("Events published by type")
                        .tag("type", eventType)
                        .register(registry)
        ).increment();
    }

    // ---- WebSocket 指标 ----

    public void recordWebSocketConnected() {
        wsConnected.set(1);
        wsConnections.increment();
    }

    public void recordWebSocketDisconnected() {
        wsConnected.set(0);
        wsDisconnections.increment();
    }

    public void recordWebSocketReconnect() {
        wsReconnects.increment();
    }

    // ---- 会话指标 ----

    public void recordSessionCreated() {
        activeSessions.incrementAndGet();
        totalSessions.incrementAndGet();
    }

    public void recordSessionClosed() {
        activeSessions.decrementAndGet();
    }

    // ---- 查询方法 ----

    public long getTotalSkillExecutions() {
        return (long) skillExecutions.count();
    }

    public long getTotalSkillErrors() {
        return (long) skillErrors.count();
    }

    public boolean isWebSocketConnected() {
        return wsConnected.get() == 1;
    }

    public int getActiveSessions() {
        return activeSessions.get();
    }
}
