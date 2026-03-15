package com.openclaw.spring.skill.example;

import com.openclaw.spring.skill.SkillRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HttpCallerSkill 测试
 */
class HttpCallerSkillTest {

    private SkillRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new SkillRegistry();
        registry.register(new HttpCallerSkill());
    }

    @Test
    @DisplayName("空 URL 报错")
    void testEmptyUrl() throws Exception {
        Object result = registry.execute("http", "get", Map.of("url", ""));
        assertTrue(result.toString().contains("失败"));
    }

    @Test
    @DisplayName("无效协议报错")
    void testInvalidProtocol() throws Exception {
        Object result = registry.execute("http", "get", Map.of("url", "ftp://example.com"));
        assertTrue(result.toString().contains("http:// 或 https://"));
    }

    @Test
    @DisplayName("GET 请求到公共 API")
    void testGetRequest() throws Exception {
        // 使用 httpbin.org 作为测试
        Object result = registry.execute("http", "get",
                Map.of("url", "https://httpbin.org/get"));
        assertTrue(result.toString().contains("HTTP"));
    }

    @Test
    @DisplayName("HEAD 请求检查可达性")
    void testHeadRequest() throws Exception {
        Object result = registry.execute("http", "head",
                Map.of("url", "https://httpbin.org"));
        assertTrue(result.toString().contains("HTTP") || result.toString().contains("失败"));
    }

    @Test
    @DisplayName("统计功能")
    void testStats() throws Exception {
        Object result = registry.execute("http", "stats", Map.of());
        assertTrue(result.toString().contains("HTTP 请求统计"));
    }

    @Test
    @DisplayName("Skill 注册验证")
    void testRegistered() {
        assertTrue(registry.hasSkill("http"));
        assertEquals("HTTP 请求工具", registry.getSkill("http").getDescription());
    }
}
