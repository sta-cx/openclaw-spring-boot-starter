package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.security.ApiKeyAuthenticationFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security 自动配置测试
 */
class SecurityAutoConfigurationTest {

    private final WebApplicationContextRunner contextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    OpenClawAutoConfiguration.class,
                    SecurityAutoConfiguration.class
            ))
            .withPropertyValues("openclaw.gateway.url=http://localhost:18789");

    @Test
    @DisplayName("security.enabled=true 时注册过滤器")
    void shouldRegisterFilterWhenSecurityEnabled() {
        contextRunner
                .withPropertyValues(
                        "openclaw.security.enabled=true",
                        "openclaw.security.api-keys=key1,key2"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(FilterRegistrationBean.class);
                    assertThat(context).hasBean("apiKeyAuthenticationFilter");
                });
    }

    @Test
    @DisplayName("security.enabled=false 时不注册过滤器")
    void shouldNotRegisterFilterWhenSecurityDisabled() {
        contextRunner
                .withPropertyValues("openclaw.security.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("apiKeyAuthenticationFilter");
                });
    }

    @Test
    @DisplayName("默认 security.enabled=false（向后兼容）")
    void shouldNotEnableSecurityByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean("apiKeyAuthenticationFilter");
                });
    }

    @Test
    @DisplayName("用户自定义过滤器优先")
    void shouldRespectCustomFilter() {
        contextRunner
                .withPropertyValues(
                        "openclaw.security.enabled=true",
                        "openclaw.security.api-keys=key1"
                )
                .withBean("customFilterRegistration", FilterRegistrationBean.class,
                        () -> {
                            FilterRegistrationBean<ApiKeyAuthenticationFilter> reg =
                                    new FilterRegistrationBean<>();
                            reg.setFilter(new ApiKeyAuthenticationFilter(
                                    new com.openclaw.spring.properties.OpenClawProperties.Security()));
                            return reg;
                        })
                .run(context -> {
                    // Custom bean should be used instead
                    assertThat(context).hasBean("customFilterRegistration");
                });
    }

    @Test
    @DisplayName("安全配置属性正确解析")
    void shouldParseSecurityProperties() {
        contextRunner
                .withPropertyValues(
                        "openclaw.security.enabled=true",
                        "openclaw.security.api-keys=abc123,def456",
                        "openclaw.security.protected-path=/custom/api"
                )
                .run(context -> {
                    var props = context.getBean(com.openclaw.spring.properties.OpenClawProperties.class);
                    assertThat(props.getSecurity().isEnabled()).isTrue();
                    assertThat(props.getSecurity().getApiKeys()).containsExactly("abc123", "def456");
                    assertThat(props.getSecurity().getProtectedPath()).isEqualTo("/custom/api");
                });
    }
}
