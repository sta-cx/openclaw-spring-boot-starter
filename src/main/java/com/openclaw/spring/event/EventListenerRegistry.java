package com.openclaw.spring.event;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 事件监听器注册表
 * 扫描 @OpenClawEventListener 注解的方法并管理事件分发
 */
public class EventListenerRegistry implements EventPublisher {

    private static final Logger log = LoggerFactory.getLogger(EventListenerRegistry.class);

    /** 按事件类型分组的监听器列表 */
    private final Map<String, List<ListenerEntry>> listeners = new ConcurrentHashMap<>();

    /** 已注册的 bean 实例（防止重复注册） */
    private final Set<Object> registeredBeans = ConcurrentHashMap.newKeySet();

    /**
     * 扫描 bean 实例中的 @OpenClawEventListener 方法并注册
     */
    public void registerListeners(Object bean) {
        if (!registeredBeans.add(bean)) {
            return; // 已注册过
        }

        Class<?> clazz = bean.getClass();
        for (Method method : clazz.getDeclaredMethods()) {
            OpenClawEventListener annotation = method.getAnnotation(OpenClawEventListener.class);
            if (annotation == null) continue;

            method.setAccessible(true);
            String[] eventTypes = annotation.value();
            int order = annotation.order();

            ListenerEntry entry = new ListenerEntry(bean, method, order);

            for (String eventType : eventTypes) {
                listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>()).add(entry);
            }

            log.info("Registered event listener: {}.{} -> {}", 
                     clazz.getSimpleName(), method.getName(), String.join(",", eventTypes));
        }

        // 排序所有列表
        for (List<ListenerEntry> list : listeners.values()) {
            list.sort(Comparator.comparingInt(e -> e.order));
        }
    }

    /**
     * 移除 bean 的所有监听器
     */
    public void unregisterListeners(Object bean) {
        registeredBeans.remove(bean);
        for (List<ListenerEntry> list : listeners.values()) {
            list.removeIf(entry -> entry.bean == bean);
        }
    }

    /**
     * 发布事件，同步分发给所有匹配的监听器
     */
    @Override
    public void publish(OpenClawEvent event) {
        List<ListenerEntry> matched = new ArrayList<>();

        // 精确匹配
        List<ListenerEntry> exact = listeners.get(event.getType());
        if (exact != null) matched.addAll(exact);

        // 通配符匹配
        List<ListenerEntry> wildcard = listeners.get(EventTypes.ALL);
        if (wildcard != null) matched.addAll(wildcard);

        if (matched.isEmpty()) return;

        // 按 order 排序
        matched.sort(Comparator.comparingInt(e -> e.order));

        for (ListenerEntry entry : matched) {
            try {
                entry.method.invoke(entry.bean, event);
            } catch (Exception e) {
                log.error("Error invoking event listener {}.{}: {}",
                          entry.bean.getClass().getSimpleName(),
                          entry.method.getName(),
                          e.getMessage(), e);
            }
        }
    }

    /**
     * 获取已注册的监听器数量
     */
    public int getListenerCount() {
        return listeners.values().stream().mapToInt(List::size).sum();
    }

    /**
     * 获取监听的事件类型
     */
    public Set<String> getEventTypes() {
        return Collections.unmodifiableSet(listeners.keySet());
    }

    /**
     * 监听器条目
     */
    private static class ListenerEntry {
        final Object bean;
        final Method method;
        final int order;

        ListenerEntry(Object bean, Method method, int order) {
            this.bean = bean;
            this.method = method;
            this.order = order;
        }
    }
}
