package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.mcp.McpToolAdapter;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Skill 自动配置 - 自动发现并注册所有 @OpenClawSkill Bean
 * 同时注册 Skill 中的 @OpenClawEventListener 方法
 */
@AutoConfiguration
public class SkillAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public SkillRegistry skillRegistry() {
        return new SkillRegistry();
    }

    @Bean
    @ConditionalOnMissingBean
    public McpToolAdapter mcpToolAdapter(SkillRegistry skillRegistry) {
        return new McpToolAdapter(skillRegistry);
    }

    @Bean
    public SkillRegistrar skillRegistrar(SkillRegistry skillRegistry, 
                                         ApplicationContext applicationContext,
                                         @ConditionalOnBean(EventListenerRegistry.class) EventListenerRegistry eventListenerRegistry) {
        return new SkillRegistrar(skillRegistry, applicationContext, eventListenerRegistry);
    }

    /**
     * 自动注册所有 @OpenClawSkill Bean
     * 同时扫描 @OpenClawEventListener 方法注册到事件系统
     */
    public static class SkillRegistrar {
        private final SkillRegistry skillRegistry;
        private final ApplicationContext applicationContext;
        private final EventListenerRegistry eventListenerRegistry;

        public SkillRegistrar(SkillRegistry skillRegistry, 
                              ApplicationContext applicationContext,
                              EventListenerRegistry eventListenerRegistry) {
            this.skillRegistry = skillRegistry;
            this.applicationContext = applicationContext;
            this.eventListenerRegistry = eventListenerRegistry;
        }

        @PostConstruct
        public void registerSkills() {
            Map<String, Object> skillBeans = applicationContext.getBeansWithAnnotation(OpenClawSkill.class);
            for (Object skillBean : skillBeans.values()) {
                skillRegistry.register(skillBean);
                // 同时注册事件监听器
                if (eventListenerRegistry != null) {
                    eventListenerRegistry.registerListeners(skillBean);
                }
            }
        }
    }
}
