package com.openclaw.spring.autoconfigure;

import com.openclaw.spring.mcp.McpToolAdapter;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillRegistry;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import jakarta.annotation.PostConstruct;
import java.util.Map;

/**
 * Skill 自动配置 - 自动发现并注册所有 @OpenClawSkill Bean
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
    public SkillRegistrar skillRegistrar(SkillRegistry skillRegistry, ApplicationContext applicationContext) {
        return new SkillRegistrar(skillRegistry, applicationContext);
    }

    /**
     * 自动注册所有 @OpenClawSkill Bean
     */
    public static class SkillRegistrar {
        private final SkillRegistry skillRegistry;
        private final ApplicationContext applicationContext;

        public SkillRegistrar(SkillRegistry skillRegistry, ApplicationContext applicationContext) {
            this.skillRegistry = skillRegistry;
            this.applicationContext = applicationContext;
        }

        @PostConstruct
        public void registerSkills() {
            Map<String, Object> skillBeans = applicationContext.getBeansWithAnnotation(OpenClawSkill.class);
            for (Object skillBean : skillBeans.values()) {
                skillRegistry.register(skillBean);
            }
        }
    }
}
