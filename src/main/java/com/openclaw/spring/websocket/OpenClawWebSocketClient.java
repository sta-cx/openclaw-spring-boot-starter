package com.openclaw.spring.websocket;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.event.EventTypes;
import com.openclaw.spring.event.OpenClawEvent;
import com.openclaw.spring.properties.OpenClawProperties;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

/**
 * OpenClaw Gateway WebSocket 客户端
 *
 * 建立与 Gateway 的 WebSocket 连接，实现：
 * - 实时事件推送（双向）
 * - 自动重连（可配置间隔和次数）
 * - 心跳保持
 * - 连接状态回调
 */
public class OpenClawWebSocketClient implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(OpenClawWebSocketClient.class);

    private final OpenClawProperties properties;
    private final EventListenerRegistry eventListenerRegistry;
    private final WebSocketClient wsClient;

    private Disposable connectionDisposable;
    private WebSocketSession currentSession;
    private final AtomicInteger reconnectCount = new AtomicInteger(0);

    private Consumer<String> onMessage;
    private Consumer<CloseStatus> onClose;
    private Runnable onConnected;
    private Consumer<Throwable> onError;

    public OpenClawWebSocketClient(OpenClawProperties properties,
                                   EventListenerRegistry eventListenerRegistry) {
        this.properties = properties;
        this.eventListenerRegistry = eventListenerRegistry;
        this.wsClient = new ReactorNettyWebSocketClient();
    }

    @PostConstruct
    public void connect() {
        if (!properties.getWebsocket().isEnabled()) {
            log.info("WebSocket client disabled by configuration");
            return;
        }

        String wsUrl = buildWebSocketUrl();
        log.info("Connecting to OpenClaw Gateway WebSocket: {}", wsUrl);

        URI uri = URI.create(wsUrl);

        connectionDisposable = wsClient.execute(uri, this)
                .retryWhen(Retry.backoff(
                        properties.getWebsocket().getMaxReconnectAttempts(),
                        Duration.ofSeconds(properties.getWebsocket().getReconnectInterval())
                )
                .doBeforeRetry(retrySignal -> {
                    int count = reconnectCount.incrementAndGet();
                    log.warn("WebSocket reconnect attempt {}/{} after {}",
                            count,
                            properties.getWebsocket().getMaxReconnectAttempts(),
                            retrySignal.totalRetriesInARow());
                    eventListenerRegistry.publish(new OpenClawEvent(
                            EventTypes.GATEWAY_RECONNECT, this, count));
                })
                .onRetryExhaustedThrow((retryBackoffSpec, retrySignal) -> {
                    log.error("WebSocket reconnection exhausted after {} attempts",
                            retrySignal.totalRetries());
                    return retrySignal.failure();
                }))
                .subscribe(
                        null,
                        error -> {
                            log.error("WebSocket connection failed: {}", error.getMessage());
                            if (onError != null) {
                                onError.accept(error);
                            }
                            eventListenerRegistry.publish(new OpenClawEvent(
                                    EventTypes.GATEWAY_DISCONNECTED, this, error.getMessage()));
                        },
                        () -> log.info("WebSocket connection closed")
                );
    }

    @PreDestroy
    public void disconnect() {
        if (connectionDisposable != null && !connectionDisposable.isDisposed()) {
            connectionDisposable.dispose();
        }
        if (currentSession != null) {
            currentSession.close(CloseStatus.NORMAL).subscribe();
        }
        log.info("WebSocket client disconnected");
    }

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        this.currentSession = session;
        reconnectCount.set(0);

        log.info("WebSocket connected to Gateway: {}", session.getId());
        eventListenerRegistry.publish(new OpenClawEvent(
                EventTypes.GATEWAY_CONNECTED, this, session.getId()));

        if (onConnected != null) {
            onConnected.run();
        }

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(this::handleMessage)
                .doOnComplete(() -> {
                    log.info("WebSocket session completed: {}", session.getId());
                    eventListenerRegistry.publish(new OpenClawEvent(
                            EventTypes.GATEWAY_DISCONNECTED, this, session.getId()));
                    if (onClose != null) {
                        onClose.accept(CloseStatus.NORMAL);
                    }
                })
                .doOnError(error -> {
                    log.error("WebSocket error: {}", error.getMessage());
                    eventListenerRegistry.publish(new OpenClawEvent(
                            EventTypes.GATEWAY_DISCONNECTED, this, error.getMessage()));
                    if (onError != null) {
                        onError.accept(error);
                    }
                })
                .then();
    }

    /**
     * 发送消息到 Gateway
     */
    public Mono<Void> send(String message) {
        if (currentSession == null || !currentSession.isOpen()) {
            return Mono.error(new IllegalStateException("WebSocket not connected"));
        }
        return currentSession.send(
                Mono.just(currentSession.textMessage(message))
        );
    }

    /**
     * 发送事件到 Gateway
     */
    public Mono<Void> sendEvent(String type, Object data) {
        String json = String.format("{\"type\":\"%s\",\"data\":%s}", type, data);
        return send(json);
    }

    private void handleMessage(String payload) {
        log.debug("WebSocket received: {}", payload);
        eventListenerRegistry.publish(new OpenClawEvent(
                EventTypes.WEBSOCKET_MESSAGE_RECEIVED, this, payload));

        if (onMessage != null) {
            onMessage.accept(payload);
        }
    }

    private String buildWebSocketUrl() {
        OpenClawProperties.Gateway gw = properties.getGateway();
        OpenClawProperties.WebSocket ws = properties.getWebsocket();

        String baseUrl = gw.getUrl()
                .replaceFirst("^http://", "ws://")
                .replaceFirst("^https://", "wss://");

        // Remove trailing slash
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        return baseUrl + ws.getPath();
    }

    // ---- 回调设置 ----

    public void setOnMessage(Consumer<String> onMessage) {
        this.onMessage = onMessage;
    }

    public void setOnClose(Consumer<CloseStatus> onClose) {
        this.onClose = onClose;
    }

    public void setOnConnected(Runnable onConnected) {
        this.onConnected = onConnected;
    }

    public void setOnError(Consumer<Throwable> onError) {
        this.onError = onError;
    }

    // ---- 状态查询 ----

    public boolean isConnected() {
        return currentSession != null && currentSession.isOpen();
    }

    public int getReconnectCount() {
        return reconnectCount.get();
    }

    public String getSessionId() {
        return currentSession != null ? currentSession.getId() : null;
    }
}
