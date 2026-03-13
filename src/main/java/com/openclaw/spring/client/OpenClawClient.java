package com.openclaw.spring.client;

import com.openclaw.spring.properties.OpenClawProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

/**
 * OpenClaw Gateway REST API Client
 * 
 * 提供与 OpenClaw Gateway 交互的核心方法
 */
public class OpenClawClient {

    private final WebClient webClient;
    private final OpenClawProperties properties;

    public OpenClawClient(OpenClawProperties properties) {
        this.properties = properties;
        
        WebClient.Builder builder = WebClient.builder()
                .baseUrl(properties.getGateway().getUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        
        if (properties.getGateway().getToken() != null) {
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, 
                    "Bearer " + properties.getGateway().getToken());
        }
        
        this.webClient = builder.build();
    }

    /**
     * 发送消息到 OpenClaw Agent
     */
    public Mono<String> sendMessage(String message) {
        return webClient.post()
                .uri("/api/chat")
                .bodyValue(Map.of("message", message))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 发送消息到指定会话
     */
    public Mono<String> sendMessage(String sessionId, String message) {
        return webClient.post()
                .uri("/api/sessions/{sessionId}/messages", sessionId)
                .bodyValue(Map.of("message", message))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 创建新会话
     */
    public Mono<Map> createSession() {
        return webClient.post()
                .uri("/api/sessions")
                .retrieve()
                .bodyToMono(Map.class);
    }

    /**
     * 列出已安装的技能
     */
    public Mono<List> listSkills() {
        return webClient.get()
                .uri("/api/skills")
                .retrieve()
                .bodyToMono(List.class);
    }

    /**
     * 执行指定技能
     */
    public Mono<String> executeSkill(String skill, String action, Map<String, Object> params) {
        return webClient.post()
                .uri("/api/skills/{skill}/execute", skill)
                .bodyValue(Map.of("action", action, "params", params))
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 获取 Gateway 状态
     */
    public Mono<Map> getStatus() {
        return webClient.get()
                .uri("/api/status")
                .retrieve()
                .bodyToMono(Map.class);
    }

    /**
     * 获取原始 WebClient（用于自定义请求）
     */
    public WebClient getWebClient() {
        return webClient;
    }
}
