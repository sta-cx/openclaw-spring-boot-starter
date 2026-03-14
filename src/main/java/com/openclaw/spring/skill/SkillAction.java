package com.openclaw.spring.skill;

import java.lang.annotation.*;

/**
 * 标注一个方法为 Skill 的可执行动作
 * 
 * 用法：
 * <pre>
 * &#64;SkillAction(name = "forecast", description = "获取天气预报")
 * public String forecast(&#64;SkillParam("city") String city) { ... }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkillAction {

    /** 动作名称 */
    String name() default "";

    /** 动作描述 */
    String description() default "";

    /** JSON Schema 定义（用于参数验证） */
    String schema() default "";
}
