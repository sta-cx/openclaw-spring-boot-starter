package com.openclaw.spring.skill;

import java.lang.annotation.*;

/**
 * 标注一个类为 OpenClaw Skill
 * 
 * 用法：
 * <pre>
 * &#64;OpenClawSkill(name = "weather", description = "天气查询")
 * public class WeatherSkill { ... }
 * </pre>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenClawSkill {

    /** Skill 名称 */
    String name();

    /** Skill 描述 */
    String description() default "";

    /** 版本号 */
    String version() default "1.0.0";

    /** 作者 */
    String author() default "";
}
