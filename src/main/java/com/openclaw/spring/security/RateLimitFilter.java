package com.openclaw.spring.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * API Rate Limiting 过滤器
 *
 * 基于滑动窗口计数器的限流算法。
 * 支持：
 * - 按 IP 限流（可扩展为按 API Key）
 * - 可配置窗口大小和最大请求数
 * - 返回 429 Too Many Requests + Retry-After header
 * - 自动清理过期窗口
 *
 * 算法：滑动窗口计数器
 * - 每个 IP 维护一个请求计数和窗口起始时间
 * - 窗口过期时重置计数
 * - 超过阈值返回 429
 *
 * 配置示例：
 *   openclaw.security.rate-limit.enabled=true
 *   openclaw.security.rate-limit.requests-per-minute=60
 *   openclaw.security.rate-limit.protected-path=/api/openclaw
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    private final int maxRequests;
    private final long windowMillis;
    private final String protectedPath;

    // IP → (窗口开始时间, 请求计数)
    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitFilter(int requestsPerMinute, String protectedPath) {
        this.maxRequests = requestsPerMinute;
        this.windowMillis = 60_000; // 1 minute
        this.protectedPath = protectedPath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIp(request);
        long now = System.currentTimeMillis();

        WindowCounter counter = counters.computeIfAbsent(clientIp, k -> new WindowCounter(now));

        synchronized (counter) {
            // 检查窗口是否过期
            if (now - counter.windowStart.get() >= windowMillis) {
                // 新窗口
                counter.windowStart.set(now);
                counter.count.set(1);
            } else {
                // 当前窗口内
                int current = counter.count.incrementAndGet();
                if (current > maxRequests) {
                    // 超限
                    long windowEnd = counter.windowStart.get() + windowMillis;
                    long retryAfter = (windowEnd - now) / 1000 + 1;

                    log.warn("Rate limit exceeded for {}: {}/{} requests in window",
                            clientIp, current, maxRequests);

                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.setHeader("Retry-After", String.valueOf(retryAfter));
                    response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
                    response.setHeader("X-RateLimit-Remaining", "0");
                    response.setHeader("X-RateLimit-Reset", String.valueOf(windowEnd / 1000));
                    response.getWriter().write(
                            String.format("{\"error\":\"RATE_LIMITED\",\"message\":\"Too many requests. Retry after %d seconds\",\"retryAfter\":%d}",
                                    retryAfter, retryAfter)
                    );
                    return;
                }
            }

            // 添加限流 headers
            int remaining = maxRequests - counter.count.get();
            response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, remaining)));
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(protectedPath);
    }

    private String getClientIp(HttpServletRequest request) {
        // 支持代理后的真实 IP
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * 获取当前限流状态（用于监控）
     */
    public Map<String, Integer> getCurrentCounts() {
        Map<String, Integer> result = new ConcurrentHashMap<>();
        long now = System.currentTimeMillis();
        counters.forEach((ip, counter) -> {
            if (now - counter.windowStart.get() < windowMillis) {
                result.put(ip, counter.count.get());
            }
        });
        return result;
    }

    /**
     * 清理过期的计数器
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        counters.entrySet().removeIf(entry ->
                now - entry.getValue().windowStart.get() >= windowMillis * 2);
    }

    private static class WindowCounter {
        final AtomicLong windowStart;
        final AtomicInteger count;

        WindowCounter(long windowStart) {
            this.windowStart = new AtomicLong(windowStart);
            this.count = new AtomicInteger(1);
        }
    }
}
