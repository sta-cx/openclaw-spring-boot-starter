package com.openclaw.spring.security;

import com.openclaw.spring.properties.OpenClawProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * API Key 认证过滤器测试
 */
class ApiKeyAuthenticationFilterTest {

    private OpenClawProperties.Security security;
    private ApiKeyAuthenticationFilter filter;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        security = new OpenClawProperties.Security();
        security.setEnabled(true);
        security.setApiKeys(List.of("test-key-123", "another-key-456"));
        security.setProtectedPath("/api/openclaw");

        filter = new ApiKeyAuthenticationFilter(security);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("有效 X-API-Key header 通过认证")
    void shouldAuthenticateWithApiKeyHeader() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");
        request.addHeader("X-API-Key", "test-key-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("有效 Bearer token 通过认证")
    void shouldAuthenticateWithBearerToken() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills/execute");
        request.addHeader("Authorization", "Bearer another-key-456");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("有效 query parameter 通过认证")
    void shouldAuthenticateWithQueryParam() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");
        request.setParameter("api_key", "test-key-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(200, response.getStatus());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("无效 Key 返回 401")
    void shouldRejectInvalidKey() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");
        request.addHeader("X-API-Key", "wrong-key");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("UNAUTHORIZED"));
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("缺少 Key 返回 401")
    void shouldRejectMissingKey() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("非保护路径不拦截")
    void shouldNotFilterUnprotectedPaths() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/health"); // Not under /api/openclaw

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("未配置 Key 时不启用认证")
    void shouldSkipAuthWhenNoKeysConfigured() throws ServletException, IOException {
        OpenClawProperties.Security noKeySecurity = new OpenClawProperties.Security();
        noKeySecurity.setProtectedPath("/api/openclaw");
        // apiKeys is empty list

        ApiKeyAuthenticationFilter noKeyFilter = new ApiKeyAuthenticationFilter(noKeySecurity);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");

        MockHttpServletResponse response = new MockHttpServletResponse();
        noKeyFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("自定义保护路径")
    void shouldUseCustomProtectedPath() throws ServletException, IOException {
        security.setProtectedPath("/admin");
        ApiKeyAuthenticationFilter customFilter = new ApiKeyAuthenticationFilter(security);

        // Request to /admin should be protected
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/config");

        MockHttpServletResponse response = new MockHttpServletResponse();
        customFilter.doFilterInternal(request, response, filterChain);

        assertEquals(401, response.getStatus());
    }

    @Test
    @DisplayName("认证成功设置 attribute")
    void shouldSetAuthenticatedAttribute() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/openclaw/skills");
        request.addHeader("X-API-Key", "test-key-123");

        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilterInternal(request, response, filterChain);

        assertEquals(true, request.getAttribute("openclaw.api.authenticated"));
    }
}
