package com.openclaw.spring.client;

import com.openclaw.spring.properties.OpenClawProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenClawClientTest {

    @Test
    void testClientCreation() {
        OpenClawProperties props = new OpenClawProperties();
        OpenClawClient client = new OpenClawClient(props);
        assertNotNull(client.getWebClient());
    }

    @Test
    void testClientWithToken() {
        OpenClawProperties props = new OpenClawProperties();
        props.getGateway().setToken("test-token");
        OpenClawClient client = new OpenClawClient(props);
        assertNotNull(client.getWebClient());
    }

    @Test
    void testClientWithCustomUrl() {
        OpenClawProperties props = new OpenClawProperties();
        props.getGateway().setUrl("http://custom:9999");
        OpenClawClient client = new OpenClawClient(props);
        assertNotNull(client.getWebClient());
    }
}
