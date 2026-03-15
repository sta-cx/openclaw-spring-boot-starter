package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.client.OpenClawClient;
import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.event.EventPublisher;
import com.openclaw.spring.mcp.McpToolAdapter;
import com.openclaw.spring.skill.SkillRegistry;
import com.openclaw.spring.skill.TestGreeterSkill;
import com.openclaw.spring.web.SkillController;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OpenClaw Spring Boot Starter 集成测试
 *
 * 使用 Spring Boot 的 ApplicationContextRunner 验证自动配置行为：
 * - Bean 正确注册
 * - 条件装配按预期工作
 * - Skill 自动发现和注册
 * - REST API 仅在 Web 环境生效
 */
class OpenClawStarterIntegrationTest {

    // ---- 基础上下文测试（非 Web） ----

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EventAutoConfiguration.class,
                    SkillAutoConfiguration.class
            ));

    @Test
    @DisplayName("自动配置 - 基础 Bean 注册")
    void shouldAutoConfigureBasicBeans() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(SkillRegistry.class);
            assertThat(context).hasSingleBean(EventListenerRegistry.class);
            assertThat(context).hasSingleBean(EventPublisher.class);
            assertThat(context).hasSingleBean(McpToolAdapter.class);
        });
    }

    @Test
    @DisplayName("自动配置 - 不在 Web 环境不注册 SkillController")
    void shouldNotRegisterControllerWhenNotWeb() {
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(SkillController.class);
        });
    }

    @Test
    @DisplayName("自动配置 - Skill 自动发现和注册")
    void shouldAutoDiscoverAndRegisterSkills() {
        contextRunner.withUserConfiguration(TestSkillConfiguration.class)
                .run(context -> {
                    SkillRegistry registry = context.getBean(SkillRegistry.class);
                    assertThat(registry.hasSkill("greeter")).isTrue();
                    assertThat(registry.getSkill("greeter").getDescription())
                            .isEqualTo("Test greeting skill");
                    assertThat(registry.getSkill("greeter").getActions())
                            .containsKeys("greet", "wave");
                });
    }

    @Test
    @DisplayName("自动配置 - Skill 执行")
    void shouldExecuteRegisteredSkill() throws Exception {
        contextRunner.withUserConfiguration(TestSkillConfiguration.class)
                .run(context -> {
                    SkillRegistry registry = context.getBean(SkillRegistry.class);
                    Object result = registry.execute("greeter", "greet",
                            java.util.Map.of("name", "J.A.R.V.I.S."));
                    assertThat(result).isEqualTo("Hello, J.A.R.V.I.S.!");

                    Object waveResult = registry.execute("greeter", "wave", java.util.Map.of());
                    assertThat(waveResult).isEqualTo("Goodbye!");
                });
    }

    @Test
    @DisplayName("自动配置 - 用户自定义 Bean 优先")
    void shouldRespectUserDefinedBeans() {
        contextRunner.withUserConfiguration(CustomRegistryConfiguration.class)
                .run(context -> {
                    // 用户自定义的 SkillRegistry 应该被使用
                    SkillRegistry registry = context.getBean(SkillRegistry.class);
                    assertThat(registry.hasSkill("custom")).isTrue();
                });
    }

    @Test
    @DisplayName("自动配置 - OpenClawClient 需要 Gateway URL")
    void shouldNotConfigureClientWithoutGatewayUrl() {
        // 没有 OpenClawAutoConfiguration（缺少 gateway.url），不注册 OpenClawClient
        contextRunner.run(context -> {
            assertThat(context).doesNotHaveBean(OpenClawClient.class);
        });
    }

    // ---- Web 上下文测试 ----

    private final WebApplicationContextRunner webContextRunner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EventAutoConfiguration.class,
                    SkillAutoConfiguration.class
            ));

    @Test
    @DisplayName("Web 环境 - SkillController 自动注册")
    void shouldRegisterControllerInWebEnvironment() {
        webContextRunner.withUserConfiguration(TestSkillConfiguration.class)
                .run(context -> {
                    assertThat(context).hasSingleBean(SkillController.class);
                });
    }

    @Test
    @DisplayName("Web 环境 - REST API 可访问")
    void shouldExposeRestEndpoints() {
        webContextRunner.withUserConfiguration(TestSkillConfiguration.class)
                .run(context -> {
                    SkillController controller = context.getBean(SkillController.class);
                    assertThat(controller).isNotNull();

                    // 验证列表接口
                    var response = controller.listSkills();
                    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
                    assertThat(response.getBody()).containsKey("skills");
                    assertThat(response.getBody()).containsKey("total");
                });
    }

    // ---- 测试配置类 ----

    @Configuration
    static class TestSkillConfiguration {
        @Bean
        public TestGreeterSkill greeterSkill() {
            return new TestGreeterSkill();
        }
    }

    @Configuration
    static class CustomRegistryConfiguration {
        @Bean
        public SkillRegistry customSkillRegistry() {
            SkillRegistry registry = new SkillRegistry();
            registry.register(new CustomSkill());
            return registry;
        }
    }

    @com.openclaw.spring.skill.OpenClawSkill(name = "custom", description = "Custom skill")
    static class CustomSkill {
        @com.openclaw.spring.skill.SkillAction(name = "run")
        public String run() {
            return "custom";
        }
    }
}
