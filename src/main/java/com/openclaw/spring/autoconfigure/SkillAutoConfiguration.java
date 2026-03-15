package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.event.EventPublisher;
import com.openclaw.spring.mcp.McpToolAdapter;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillRegistry;
import com.openclaw.spring.web.SkillController;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Skill 自动配置 - 自动发现并注册所有 @OpenClawSkill Bean
 * 同时注册 Skill 中的 @OpenClawEventListener 方法
 */
@AutoConfiguration(after = {EventAutoConfiguration.class})
@AutoConfigureAfter(EventAutoConfiguration.class)
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
                                         ObjectProvider<EventListenerRegistry> eventListenerRegistry) {
        return new SkillRegistrar(skillRegistry, applicationContext, eventListenerRegistry);
    }

    /**
     * REST API 控制器（仅 Web 应用生效）
     */
    @Bean
    @ConditionalOnWebApplication
    @ConditionalOnClass(name = "org.springframework.web.bind.annotation.RestController")
    @ConditionalOnMissingBean
    public SkillController skillController(SkillRegistry skillRegistry) {
        return new SkillController(skillRegistry);
    }

    /**
     * 自动注册所有 @OpenClawSkill Bean
     * 同时扫描 @OpenClawEventListener 方法注册到事件系统
     */
    public static class SkillRegistrar {
        private final SkillRegistry skillRegistry;
        private final ApplicationContext applicationContext;
        private final ObjectProvider<EventListenerRegistry> eventListenerRegistryProvider;

        public SkillRegistrar(SkillRegistry skillRegistry,
                              ApplicationContext applicationContext,
                              ObjectProvider<EventListenerRegistry> eventListenerRegistryProvider) {
            this.skillRegistry = skillRegistry;
            this.applicationContext = applicationContext;
            this.eventListenerRegistryProvider = eventListenerRegistryProvider;
        }

        @PostConstruct
        public void registerSkills() {
            Map<String, Object> skillBeans = applicationContext.getBeansWithAnnotation(OpenClawSkill.class);
            EventListenerRegistry eventListenerRegistry = eventListenerRegistryProvider.getIfAvailable();
            for (Object skillBean : skillBeans.values()) {
                OpenClawSkill annotation = skillBean.getClass().getAnnotation(OpenClawSkill.class);
                skillRegistry.register(skillBean);

                // 同时注册事件监听器（如果事件系统可用）
                if (eventListenerRegistry != null) {
                    eventListenerRegistry.registerListeners(skillBean);
                    // 发布 Skill 注册事件
                    eventListenerRegistry.publish(new com.openclaw.spring.event.OpenClawEvent(
                            com.openclaw.spring.event.EventTypes.SKILL_REGISTERED,
                            skillBean,
                            Map.of("name", annotation.name(),
                                   "version", annotation.version(),
                                   "description", annotation.description())
                    ));
                }
            }
        }
    }
}
