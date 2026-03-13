package com.openclaw.spring.properties;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenClawPropertiesTest {

    @Test
    void testDefaults() {
        OpenClawProperties props = new OpenClawProperties();
        assertEquals("http://localhost:18789", props.getGateway().getUrl());
        assertNull(props.getGateway().getToken());
        assertEquals(30, props.getGateway().getTimeout());
    }

    @Test
    void testCustomValues() {
        OpenClawProperties props = new OpenClawProperties();
        props.getGateway().setUrl("http://192.168.1.100:18789");
        props.getGateway().setToken("test-token");
        props.getGateway().setTimeout(60);

        assertEquals("http://192.168.1.100:18789", props.getGateway().getUrl());
        assertEquals("test-token", props.getGateway().getToken());
        assertEquals(60, props.getGateway().getTimeout());
    }
}
