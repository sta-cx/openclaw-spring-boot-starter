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
     * 注销一个 Skill
     *
     * @param skillName 要注销的 Skill 名称
     * @return 被注销的 Skill，如果不存在返回 null
     */
    public RegisteredSkill unregister(String skillName) {
        return skills.remove(skillName);
    }

    /**
     * 检查是否可以注销 Skill（根据实例匹配）
     *
     * @param skillInstance Skill 实例
     * @return 被注销的 Skill，如果实例不匹配返回 null
     */
    public RegisteredSkill unregisterByInstance(Object skillInstance) {
        String nameToRemove = null;
        for (var entry : skills.entrySet()) {
            if (entry.getValue().getInstance() == skillInstance) {
                nameToRemove = entry.getKey();
                break;
            }
        }
        return nameToRemove != null ? skills.remove(nameToRemove) : null;
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

        if (params == null) {
            params = Map.of();
        }

        Class<?>[] paramTypes = method.getParameterTypes();
        java.lang.reflect.Parameter[] parameters = method.getParameters();

        if (paramTypes.length == 0) {
            // 无参方法
            return method.invoke(skill.getInstance());
        } else if (paramTypes.length == 1) {
            // 单参数：优先按 @SkillParam name 映射，fallback 到第一个 value
            SkillParam skillParam = parameters[0].getAnnotation(SkillParam.class);
            Object value;
            if (skillParam != null && params.containsKey(skillParam.value())) {
                value = params.get(skillParam.value());
            } else if (!params.isEmpty()) {
                value = params.values().iterator().next();
            } else {
                value = null;
            }
            return method.invoke(skill.getInstance(), value);
        } else {
            // 多参数：按 @SkillParam name 映射
            Object[] args = new Object[paramTypes.length];
            for (int i = 0; i < parameters.length; i++) {
                SkillParam skillParam = parameters[i].getAnnotation(SkillParam.class);
                String paramName = skillParam != null ? skillParam.value() : parameters[i].getName();
                args[i] = params.get(paramName);
            }
            return method.invoke(skill.getInstance(), args);
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
