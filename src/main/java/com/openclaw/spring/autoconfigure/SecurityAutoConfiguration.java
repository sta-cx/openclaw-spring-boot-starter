package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.properties.OpenClawProperties;
import com.openclaw.spring.security.ApiKeyAuthenticationFilter;
import com.openclaw.spring.security.RateLimitFilter;
import jakarta.servlet.Filter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

/**
 * 安全自动配置
 *
 * 提供两个独立的安全过滤器：
 * 1. API Key 认证（openclaw.security.enabled=true）
 * 2. Rate Limiting（openclaw.security.rate-limit.enabled=true）
 *
 * 两个过滤器可独立启用，互不依赖。
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(Filter.class)
public class SecurityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "openclaw.security", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<ApiKeyAuthenticationFilter> apiKeyAuthenticationFilter(
            OpenClawProperties properties) {

        OpenClawProperties.Security security = properties.getSecurity();
        ApiKeyAuthenticationFilter filter = new ApiKeyAuthenticationFilter(security);

        FilterRegistrationBean<ApiKeyAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns(security.getProtectedPath() + "/*");
        registration.setOrder(100);
        registration.setName("openclawApiKeyFilter");

        return registration;
    }

    @Bean
    @ConditionalOnMissingBean(name = "rateLimitFilterRegistration")
    @ConditionalOnProperty(prefix = "openclaw.security.rate-limit", name = "enabled", havingValue = "true")
    public FilterRegistrationBean<RateLimitFilter> rateLimitFilterRegistration(
            OpenClawProperties properties) {

        OpenClawProperties.Security security = properties.getSecurity();
        OpenClawProperties.Security.RateLimit rateLimit = security.getRateLimit();

        RateLimitFilter filter = new RateLimitFilter(
                rateLimit.getRequestsPerMinute(),
                security.getProtectedPath()
        );

        FilterRegistrationBean<RateLimitFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(filter);
        registration.addUrlPatterns(security.getProtectedPath() + "/*");
        registration.setOrder(90); // Runs before API Key filter
        registration.setName("openclawRateLimitFilter");

        return registration;
    }
}
