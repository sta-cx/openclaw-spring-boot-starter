package com.openclaw.spring.security;

import com.openclaw.spring.properties.OpenClawProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * API Key 认证过滤器
 *
 * 保护 /api/openclaw/** 端点，要求请求携带有效的 API Key。
 * 支持以下方式传递 Key：
 * - Header: X-API-Key
 * - Header: Authorization: Bearer <key>
 * - Query param: ?api_key=<key>
 *
 * 多 Key 支持：可配置多个有效 Key，用于多客户端场景。
 * 如果未配置任何 Key，过滤器自动跳过（向后兼容）。
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthenticationFilter.class);

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_KEY_PARAM = "api_key";
    public static final String BEARER_PREFIX = "Bearer ";

    private final List<String> validApiKeys;
    private final String protectedPath;

    public ApiKeyAuthenticationFilter(OpenClawProperties.Security security) {
        this.validApiKeys = security.getApiKeys();
        this.protectedPath = security.getProtectedPath();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 没有配置 Key → 不启用认证（向后兼容）
        if (validApiKeys == null || validApiKeys.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        // 提取请求中的 Key
        String apiKey = extractApiKey(request);

        // 验证
        if (apiKey == null || !validApiKeys.contains(apiKey)) {
            log.warn("Unauthorized API access attempt from {}: {} {}",
                    request.getRemoteAddr(), request.getMethod(), request.getRequestURI());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"UNAUTHORIZED\",\"message\":\"Valid API key required. " +
                    "Provide via X-API-Key header, Authorization: Bearer, or api_key query parameter.\"}"
            );
            return;
        }

        // 认证通过
        log.debug("API key authenticated for {} {}", request.getMethod(), request.getRequestURI());
        request.setAttribute("openclaw.api.authenticated", true);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // 只保护指定路径
        String path = request.getRequestURI();
        return !path.startsWith(protectedPath);
    }

    private String extractApiKey(HttpServletRequest request) {
        // 1. X-API-Key header
        String key = request.getHeader(API_KEY_HEADER);
        if (StringUtils.hasText(key)) {
            return key;
        }

        // 2. Authorization: Bearer <key>
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }

        // 3. Query parameter
        key = request.getParameter(API_KEY_PARAM);
        if (StringUtils.hasText(key)) {
            return key;
        }

        return null;
    }
}
