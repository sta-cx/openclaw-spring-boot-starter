package com.openclaw.spring.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @Test
    void testDefaultConstructor() {
        Session session = new Session();
        assertNull(session.getId());
        assertNull(session.getLabel());
    }

    @Test
    void testSetters() {
        Session session = new Session();
        session.setId("sess-001");
        session.setLabel("test-session");
        session.setCreatedAt("2026-03-13T10:00:00Z");
        session.setLastActiveAt("2026-03-13T10:05:00Z");

        assertEquals("sess-001", session.getId());
        assertEquals("test-session", session.getLabel());
        assertNotNull(session.getCreatedAt());
        assertNotNull(session.getLastActiveAt());
    }
}
