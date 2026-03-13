package com.openclaw.spring.skill;

import java.lang.reflect.Method;
import java.util.*;

/**
 * Skill 注册表 - 管理所有已注册的 Skill
 */
public class SkillRegistry {

    private final Map<String, RegisteredSkill> skills = new LinkedHashMap<>();

    /**
     * 注册一个 Skill 实例
     */
    public void register(Object skillInstance) {
        OpenClawSkill annotation = skillInstance.getClass().getAnnotation(OpenClawSkill.class);
        if (annotation == null) {
            throw new IllegalArgumentException("Class must be annotated with @OpenClawSkill");
        }

        String skillName = annotation.name();
        Map<String, SkillActionInfo> actions = new LinkedHashMap<>();

        for (Method method : skillInstance.getClass().getDeclaredMethods()) {
            SkillAction actionAnnotation = method.getAnnotation(SkillAction.class);
            if (actionAnnotation != null) {
                String actionName = actionAnnotation.name().isEmpty() 
                    ? method.getName() 
                    : actionAnnotation.name();
                actions.put(actionName, new SkillActionInfo(actionName, actionAnnotation.description(), method));
            }
        }

        skills.put(skillName, new RegisteredSkill(skillName, annotation.description(), 
                                                   annotation.version(), skillInstance, actions));
    }

    /**
     * 获取所有已注册的 Skill
     */
    public Map<String, RegisteredSkill> getSkills() {
        return Collections.unmodifiableMap(skills);
    }

    /**
     * 获取指定 Skill
     */
    public RegisteredSkill getSkill(String name) {
        return skills.get(name);
    }

    /**
     * 检查 Skill 是否存在
     */
    public boolean hasSkill(String name) {
        return skills.containsKey(name);
    }

    /**
     * 执行 Skill 动作
     */
    public Object execute(String skillName, String actionName, Map<String, Object> params) throws Exception {
        RegisteredSkill skill = skills.get(skillName);
        if (skill == null) {
            throw new IllegalArgumentException("Skill not found: " + skillName);
        }

        SkillActionInfo action = skill.getActions().get(actionName);
        if (action == null) {
            throw new IllegalArgumentException("Action not found: " + actionName + " in skill: " + skillName);
        }

        Method method = action.getMethod();
        method.setAccessible(true);

        // 简单参数映射
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length == 0) {
            return method.invoke(skill.getInstance());
        } else if (paramTypes.length == 1 && params != null && !params.isEmpty()) {
            Object firstValue = params.values().iterator().next();
            return method.invoke(skill.getInstance(), firstValue);
        } else {
            return method.invoke(skill.getInstance(), params);
        }
    }

    /**
     * 已注册的 Skill 信息
     */
    public static class RegisteredSkill {
        private final String name;
        private final String description;
        private final String version;
        private final Object instance;
        private final Map<String, SkillActionInfo> actions;

        public RegisteredSkill(String name, String description, String version, 
                              Object instance, Map<String, SkillActionInfo> actions) {
            this.name = name;
            this.description = description;
            this.version = version;
            this.instance = instance;
            this.actions = actions;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getVersion() { return version; }
        public Object getInstance() { return instance; }
        public Map<String, SkillActionInfo> getActions() { return actions; }
    }

    /**
     * Skill 动作信息
     */
    public static class SkillActionInfo {
        private final String name;
        private final String description;
        private final Method method;

        public SkillActionInfo(String name, String description, Method method) {
            this.name = name;
            this.description = description;
            this.method = method;
        }

        public String getName() { return name; }
        public String getDescription() { return description; }
        public Method getMethod() { return method; }
    }
}
