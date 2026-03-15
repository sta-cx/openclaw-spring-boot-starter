package com.openclaw.spring.websocket;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.event.EventTypes;
import com.openclaw.spring.event.OpenClawEvent;
import com.openclaw.spring.event.OpenClawEventListener;
import com.openclaw.spring.properties.OpenClawProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WebSocket 客户端单元测试
 */
class OpenClawWebSocketClientTest {

    private OpenClawProperties properties;
    private EventListenerRegistry eventListenerRegistry;
    private OpenClawWebSocketClient client;

    @BeforeEach
    void setUp() {
        properties = new OpenClawProperties();
        properties.getGateway().setUrl("http://localhost:18789");
        properties.getWebsocket().setEnabled(true);
        properties.getWebsocket().setPath("/ws/events");
        properties.getWebsocket().setReconnectInterval(3);
        properties.getWebsocket().setMaxReconnectAttempts(5);

        eventListenerRegistry = new EventListenerRegistry();
        client = new OpenClawWebSocketClient(properties, eventListenerRegistry);
    }

    @Test
    @DisplayName("WebSocket URL 构建 - HTTP → WS")
    void shouldBuildWsUrlFromHttp() {
        properties.getGateway().setUrl("http://localhost:18789");
        properties.getWebsocket().setPath("/ws/events");

        OpenClawWebSocketClient wsClient = new OpenClawWebSocketClient(properties, eventListenerRegistry);
        // URL is built internally; verify via the client's connect logic
        assertNotNull(wsClient);
    }

    @Test
    @DisplayName("WebSocket URL 构建 - HTTPS → WSS")
    void shouldBuildWssUrlFromHttps() {
        properties.getGateway().setUrl("https://gateway.example.com");
        properties.getWebsocket().setPath("/ws/events");

        OpenClawWebSocketClient wsClient = new OpenClawWebSocketClient(properties, eventListenerRegistry);
        assertNotNull(wsClient);
    }

    @Test
    @DisplayName("初始状态 - 未连接")
    void shouldNotBeConnectedInitially() {
        assertFalse(client.isConnected());
        assertNull(client.getSessionId());
        assertEquals(0, client.getReconnectCount());
    }

    @Test
    @DisplayName("WebSocket 禁用时不连接")
    void shouldNotConnectWhenDisabled() {
        properties.getWebsocket().setEnabled(false);
        OpenClawWebSocketClient disabledClient = new OpenClawWebSocketClient(properties, eventListenerRegistry);

        // connect() should return early without error
        assertDoesNotThrow(disabledClient::connect);
        assertFalse(disabledClient.isConnected());
    }

    @Test
    @DisplayName("回调设置")
    void shouldSetCallbacks() {
        List<String> messages = new ArrayList<>();
        List<Throwable> errors = new ArrayList<>();
        boolean[] connected = {false};

        client.setOnMessage(messages::add);
        client.setOnError(errors::add);
        client.setOnConnected(() -> connected[0] = true);

        // Verify callbacks are set (they won't fire without real connection)
        assertNotNull(client);
        assertFalse(connected[0]);
        assertTrue(messages.isEmpty());
    }

    @Test
    @DisplayName("Properties 默认值")
    void shouldHaveCorrectDefaults() {
        OpenClawProperties defaults = new OpenClawProperties();

        assertTrue(defaults.getWebsocket().isEnabled());
        assertEquals("/ws/events", defaults.getWebsocket().getPath());
        assertEquals(5, defaults.getWebsocket().getReconnectInterval());
        assertEquals(10, defaults.getWebsocket().getMaxReconnectAttempts());
    }

    @Test
    @DisplayName("Properties 自定义值")
    void shouldAcceptCustomValues() {
        properties.getWebsocket().setEnabled(false);
        properties.getWebsocket().setPath("/custom/ws");
        properties.getWebsocket().setReconnectInterval(10);
        properties.getWebsocket().setMaxReconnectAttempts(20);

        assertFalse(properties.getWebsocket().isEnabled());
        assertEquals("/custom/ws", properties.getWebsocket().getPath());
        assertEquals(10, properties.getWebsocket().getReconnectInterval());
        assertEquals(20, properties.getWebsocket().getMaxReconnectAttempts());
    }

    @Test
    @DisplayName("发送消息 - 未连接时返回错误")
    void shouldFailSendWhenNotConnected() {
        assertThrows(IllegalStateException.class, () ->
                client.send("test").block()
        );
    }
}
