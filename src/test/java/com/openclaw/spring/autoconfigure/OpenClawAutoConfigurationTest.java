package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.client.OpenClawClient;
import com.openclaw.spring.properties.OpenClawProperties;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OpenClawAutoConfigurationTest {

    @Test
    void testBeanCreation() {
        OpenClawProperties props = new OpenClawProperties();
        OpenClawAutoConfiguration config = new OpenClawAutoConfiguration();
        OpenClawClient client = config.openClawClient(props);
        assertNotNull(client);
    }

    @Test
    void testPropertiesInjection() {
        OpenClawProperties props = new OpenClawProperties();
        props.getGateway().setUrl("http://test:18789");
        props.getGateway().setToken("abc123");

        OpenClawAutoConfiguration config = new OpenClawAutoConfiguration();
        OpenClawClient client = config.openClawClient(props);
        assertNotNull(client);
    }
}
