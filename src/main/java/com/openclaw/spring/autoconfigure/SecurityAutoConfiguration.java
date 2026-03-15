package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.properties.OpenClawProperties;
import com.openclaw.spring.security.ApiKeyAuthenticationFilter;
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
 * 当满足以下条件时启用 API Key 认证：
 * - Web 应用环境
 * - classpath 中存在 jakarta.servlet.Filter
 * - openclaw.security.enabled=true
 * - openclaw.security.api-keys 已配置且非空
 *
 * 如果未启用，过滤器不注册，所有端点保持开放（向后兼容）。
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
        registration.setOrder(100); // Low priority number = higher precedence
        registration.setName("openclawApiKeyFilter");

        return registration;
    }
}
