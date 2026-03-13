package com.openclaw.spring.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenClawMessageTest {

    @Test
    void testUserMessage() {
        OpenClawMessage msg = OpenClawMessage.user("Hello");
        assertEquals("user", msg.getRole());
        assertEquals("Hello", msg.getContent());
        assertTrue(msg.getTimestamp() > 0);
    }

    @Test
    void testAssistantMessage() {
        OpenClawMessage msg = OpenClawMessage.assistant("Hi there");
        assertEquals("assistant", msg.getRole());
        assertEquals("Hi there", msg.getContent());
    }

    @Test
    void testCustomConstructor() {
        OpenClawMessage msg = new OpenClawMessage("system", "init");
        assertEquals("system", msg.getRole());
        assertEquals("init", msg.getContent());
    }

    @Test
    void testSetters() {
        OpenClawMessage msg = new OpenClawMessage();
        msg.setId("msg-001");
        msg.setRole("user");
        msg.setContent("test");
        msg.setTimestamp(1700000000000L);

        assertEquals("msg-001", msg.getId());
        assertEquals("user", msg.getRole());
        assertEquals("test", msg.getContent());
        assertEquals(1700000000000L, msg.getTimestamp());
    }
}
