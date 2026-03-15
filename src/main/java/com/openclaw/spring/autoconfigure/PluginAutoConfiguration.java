package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.plugin.SkillPluginLoader;
import com.openclaw.spring.properties.OpenClawProperties;
import com.openclaw.spring.skill.SkillRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

/**
 * Skill 插件自动配置
 *
 * 当满足以下条件时启用：
 * - openclaw.plugin.enabled=true
 *
 * 自动行为：
 * 1. 创建 SkillPluginLoader
 * 2. 扫描插件目录加载所有 JAR
 * 3. 如果 hotReload=true，启动目录监控
 */
@AutoConfiguration(after = SkillAutoConfiguration.class)
public class PluginAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(PluginAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "openclaw.plugin", name = "enabled", havingValue = "true")
    @DependsOn("skillRegistry")
    public SkillPluginLoader skillPluginLoader(
            OpenClawProperties properties,
            SkillRegistry skillRegistry) {

        OpenClawProperties.Plugin pluginConfig = properties.getPlugin();
        SkillPluginLoader loader = new SkillPluginLoader(skillRegistry, pluginConfig.getDirectory());

        // Initial scan
        loader.scanAndLoad();

        // Start hot reload watcher
        if (pluginConfig.isHotReload()) {
            loader.startWatching();
            log.info("Skill plugin hot-reload enabled for: {}", pluginConfig.getDirectory());
        }

        return loader;
    }
}
