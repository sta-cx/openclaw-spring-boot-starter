package com.openclaw.spring.event;

/**
 * 事件发布器接口
 */
public interface EventPublisher {

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
        new Thread(() -> publish(event)).start();
    }

    /**
     * 异步发布事件（按类型快捷方式）
     */
    default void publishAsync(String type, Object source, Object data) {
        publishAsync(new OpenClawEvent(type, source, data));
    }
}
