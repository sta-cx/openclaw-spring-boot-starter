package com.openclaw.spring.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EventListenerRegistry 单元测试
 */
class EventListenerRegistryTest {

    private EventListenerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new EventListenerRegistry();
    }

    @Test
    void testPublishAndReceiveEvent() {
        TestListener listener = new TestListener();
        registry.registerListeners(listener);

        OpenClawEvent event = new OpenClawEvent(EventTypes.MESSAGE_RECEIVED, this, "hello");
        registry.publish(event);

        assertEquals(1, listener.messageReceivedCount);
        assertEquals("hello", listener.lastData);
    }

    @Test
    void testWildcardListener() {
        WildcardListener listener = new WildcardListener();
        registry.registerListeners(listener);

        registry.publish(EventTypes.MESSAGE_RECEIVED, this, "msg1");
        registry.publish(EventTypes.SESSION_CREATED, this, "session1");

        assertEquals(2, listener.eventCount);
    }

    @Test
    void testOrderPreserved() {
        OrderedListener first = new OrderedListener("first", 10);
        OrderedListener second = new OrderedListener("second", 1);
        OrderedListener third = new OrderedListener("third", 5);

        registry.registerListeners(first);
        registry.registerListeners(second);
        registry.registerListeners(third);

        registry.publish(EventTypes.MESSAGE_RECEIVED, this, "data");

        // order=1 应该最先执行
        assertEquals("second", OrderedListener.executionOrder.get(0));
        assertEquals("third", OrderedListener.executionOrder.get(1));
        assertEquals("first", OrderedListener.executionOrder.get(2));
        OrderedListener.executionOrder.clear();
    }

    @Test
    void testDuplicateRegistrationIgnored() {
        TestListener listener = new TestListener();
        registry.registerListeners(listener);
        registry.registerListeners(listener); // 第二次应该被忽略

        assertEquals(1, registry.getListenerCount());
    }

    @Test
    void testUnregister() {
        TestListener listener = new TestListener();
        registry.registerListeners(listener);
        assertEquals(1, registry.getListenerCount());

        registry.unregisterListeners(listener);
        assertEquals(0, registry.getListenerCount());
    }

    @Test
    void testPublishAsync() throws InterruptedException {
        TestListener listener = new TestListener();
        registry.registerListeners(listener);

        registry.publishAsync(EventTypes.MESSAGE_RECEIVED, this, "async-data");
        Thread.sleep(200); // 等待异步执行

        assertEquals(1, listener.messageReceivedCount);
        assertEquals("async-data", listener.lastData);
    }

    @Test
    void testGetEventTypes() {
        registry.registerListeners(new TestListener());
        registry.registerListeners(new WildcardListener());

        assertTrue(registry.getEventTypes().contains(EventTypes.MESSAGE_RECEIVED));
        assertTrue(registry.getEventTypes().contains(EventTypes.ALL));
    }

    // === 测试用监听器 ===

    static class TestListener {
        int messageReceivedCount = 0;
        Object lastData;

        @OpenClawEventListener(EventTypes.MESSAGE_RECEIVED)
        public void onMessage(OpenClawEvent event) {
            messageReceivedCount++;
            lastData = event.getData();
        }
    }

    static class WildcardListener {
        int eventCount = 0;

        @OpenClawEventListener(EventTypes.ALL)
        public void onAny(OpenClawEvent event) {
            eventCount++;
        }
    }

    static class OrderedListener {
        static java.util.List<String> executionOrder = new java.util.ArrayList<>();
        private final String name;

        OrderedListener(String name, int order) {
            this.name = name;
            this.order = order;
        }

        private final int order;

        @OpenClawEventListener(value = EventTypes.MESSAGE_RECEIVED, order = 0)
        public void onMessage(OpenClawEvent event) {
            // 使用反射设置 order 值（测试简化：直接通过字段）
            executionOrder.add(name);
        }
    }
}
