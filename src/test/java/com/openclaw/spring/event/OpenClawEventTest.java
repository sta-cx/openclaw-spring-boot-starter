package com.openclaw.spring.event;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * OpenClawEvent 测试
 */
class OpenClawEventTest {

    @Test
    void testEventCreation() {
        String source = "test-source";
        String data = "test-data";
        OpenClawEvent event = new OpenClawEvent(EventTypes.MESSAGE_RECEIVED, source, data);

        assertEquals(EventTypes.MESSAGE_RECEIVED, event.getType());
        assertEquals(source, event.getSource());
        assertEquals(data, event.getData());
        assertTrue(event.getTimestamp() > 0);
    }

    @Test
    void testEventToString() {
        OpenClawEvent event = new OpenClawEvent("test.type", this, null);
        String str = event.toString();
        assertTrue(str.contains("test.type"));
    }

    @Test
    void testTimestampIsRecent() {
        long before = System.currentTimeMillis();
        OpenClawEvent event = new OpenClawEvent("test", this, null);
        long after = System.currentTimeMillis();

        assertTrue(event.getTimestamp() >= before);
        assertTrue(event.getTimestamp() <= after);
    }
}
