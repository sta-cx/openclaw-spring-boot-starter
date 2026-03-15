package com.openclaw.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Rate Limiting 过滤器测试
 */
class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        // 3 requests per minute for testing
        filter = new RateLimitFilter(3, "/api/openclaw");
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("正常请求通过")
    void shouldAllowNormalRequests() throws ServletException, IOException {
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/openclaw/skills");
            request.setRemoteAddr("192.168.1.1");

            MockHttpServletResponse response = new MockHttpServletResponse();
            filter.doFilterInternal(request, response, filterChain);

            assertEquals(200, response.getStatus());
            assertEquals("3", response.getHeader("X-RateLimit-Limit"));
            verify(filterChain, times(i + 1)).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("超过限制返回 429")
    void shouldRejectWhenExceeded() throws ServletException, IOException {
        // Use up the quota
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.setRequestURI("/api/openclaw/skills");
            request.setRemoteAddr("10.0.0.1");
            filter.doFilterInternal(request, new MockHttpServletResponse(), filterChain);
        }

        // This one should be rejected
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");
        request.setRemoteAddr("10.0.0.1");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(429, response.getStatus());
        assertTrue(response.getContentAsString().contains("RATE_LIMITED"));
        assertNotNull(response.getHeader("Retry-After"));
        assertEquals("0", response.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    @DisplayName("不同 IP 独立计算")
    void shouldTrackSeparatelyByIp() throws ServletException, IOException {
        // IP1 uses all quota
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/openclaw/skills");
            req.setRemoteAddr("10.0.0.1");
            filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        // IP2 should still work
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/openclaw/skills");
        req.setRemoteAddr("10.0.0.2");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(req, response, filterChain);

        assertEquals(200, response.getStatus());
    }

    @Test
    @DisplayName("非保护路径不拦截")
    void shouldNotFilterUnprotectedPaths() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/health");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("X-Forwarded-For 头解析")
    void shouldUseForwardedForHeader() throws ServletException, IOException {
        // Use quota for the forwarded IP
        for (int i = 0; i < 3; i++) {
            MockHttpServletRequest req = new MockHttpServletRequest();
            req.setRequestURI("/api/openclaw/skills");
            req.setRemoteAddr("127.0.0.1");
            req.addHeader("X-Forwarded-For", "203.0.113.50");
            filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);
        }

        // Same forwarded IP should be rate limited
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/openclaw/skills");
        req.setRemoteAddr("127.0.0.1");
        req.addHeader("X-Forwarded-For", "203.0.113.50");
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(req, response, filterChain);

        assertEquals(429, response.getStatus());
    }

    @Test
    @DisplayName("剩余配额 header 正确")
    void shouldSetRemainingHeader() throws ServletException, IOException {
        // First request
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        req1.setRequestURI("/api/openclaw/skills");
        req1.setRemoteAddr("172.16.0.1");
        MockHttpServletResponse resp1 = new MockHttpServletResponse();
        filter.doFilterInternal(req1, resp1, filterChain);
        assertEquals("2", resp1.getHeader("X-RateLimit-Remaining"));

        // Second request
        MockHttpServletRequest req2 = new MockHttpServletRequest();
        req2.setRequestURI("/api/openclaw/skills");
        req2.setRemoteAddr("172.16.0.1");
        MockHttpServletResponse resp2 = new MockHttpServletResponse();
        filter.doFilterInternal(req2, resp2, filterChain);
        assertEquals("1", resp2.getHeader("X-RateLimit-Remaining"));

        // Third request
        MockHttpServletRequest req3 = new MockHttpServletRequest();
        req3.setRequestURI("/api/openclaw/skills");
        req3.setRemoteAddr("172.16.0.1");
        MockHttpServletResponse resp3 = new MockHttpServletResponse();
        filter.doFilterInternal(req3, resp3, filterChain);
        assertEquals("0", resp3.getHeader("X-RateLimit-Remaining"));
    }

    @Test
    @DisplayName("当前计数查询")
    void shouldGetCurrentCounts() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/openclaw/skills");
        req.setRemoteAddr("192.168.1.100");
        filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);

        var counts = filter.getCurrentCounts();
        assertTrue(counts.containsKey("192.168.1.100"));
        assertEquals(1, counts.get("192.168.1.100"));
    }

    @Test
    @DisplayName("清理过期计数器")
    void shouldCleanupExpiredCounters() throws ServletException, IOException {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setRequestURI("/api/openclaw/skills");
        req.setRemoteAddr("10.10.10.10");
        filter.doFilterInternal(req, new MockHttpServletResponse(), filterChain);

        assertDoesNotThrow(() -> filter.cleanup());
    }
}
