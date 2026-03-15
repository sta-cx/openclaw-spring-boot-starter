package com.openclaw.spring.event;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 事件发布器接口
 */
public interface EventPublisher {

    /** 共享线程池用于异步发布 */
    Executor ASYNC_EXECUTOR = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() / 2),
            r -> {
                Thread t = new Thread(r, "openclaw-event-async");
                t.setDaemon(true);
                return t;
            }
    );

    /**
     * 发布事件（同步）
     */
    void publish(OpenClawEvent event);

    /**
     * 发布事件（按类型快捷方式）
     */
    default void publish(String type, Object source, Object data) {
        publish(new OpenClawEvent(type, source, data));
    }

    /**
     * 异步发布事件
     */
    default void publishAsync(OpenClawEvent event) {
        CompletableFuture.runAsync(() -> publish(event), ASYNC_EXECUTOR);
    }

    /**
     * 异步发布事件（按类型快捷方式）
     */
    default void publishAsync(String type, Object source, Object data) {
        publishAsync(new OpenClawEvent(type, source, data));
    }
}
