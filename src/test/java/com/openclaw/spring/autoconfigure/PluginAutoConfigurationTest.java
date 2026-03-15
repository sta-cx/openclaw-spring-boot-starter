package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.plugin.SkillPluginLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Plugin 自动配置测试
 */
class PluginAutoConfigurationTest {

    @TempDir
    Path tempDir;

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    EventAutoConfiguration.class,
                    SkillAutoConfiguration.class,
                    PluginAutoConfiguration.class
            ));

    @Test
    @DisplayName("plugin.enabled=true 时注册加载器")
    void shouldRegisterLoaderWhenEnabled() {
        contextRunner
                .withPropertyValues(
                        "openclaw.plugin.enabled=true",
                        "openclaw.plugin.directory=" + tempDir.toString()
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(SkillPluginLoader.class);
                });
    }

    @Test
    @DisplayName("plugin.enabled=false 时不注册加载器")
    void shouldNotRegisterLoaderWhenDisabled() {
        contextRunner
                .withPropertyValues("openclaw.plugin.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SkillPluginLoader.class);
                });
    }

    @Test
    @DisplayName("默认 plugin.enabled=false")
    void shouldNotEnablePluginsByDefault() {
        contextRunner
                .run(context -> {
                    assertThat(context).doesNotHaveBean(SkillPluginLoader.class);
                });
    }

    @Test
    @DisplayName("用户自定义加载器优先")
    void shouldRespectCustomLoader() {
        contextRunner
                .withPropertyValues("openclaw.plugin.enabled=true")
                .withBean("customLoader", SkillPluginLoader.class,
                        () -> new SkillPluginLoader(
                                new com.openclaw.spring.skill.SkillRegistry(),
                                tempDir.toString()))
                .run(context -> {
                    assertThat(context).hasSingleBean(SkillPluginLoader.class);
                    assertThat(context).hasBean("customLoader");
                });
    }

    @Test
    @DisplayName("插件配置属性正确解析")
    void shouldParsePluginProperties() {
        contextRunner
                .withPropertyValues(
                        "openclaw.plugin.enabled=true",
                        "openclaw.plugin.directory=/custom/plugins",
                        "openclaw.plugin.hot-reload=false"
                )
                .run(context -> {
                    var props = context.getBean(com.openclaw.spring.properties.OpenClawProperties.class);
                    assertThat(props.getPlugin().isEnabled()).isTrue();
                    assertThat(props.getPlugin().getDirectory()).isEqualTo("/custom/plugins");
                    assertThat(props.getPlugin().isHotReload()).isFalse();
                });
    }
}
