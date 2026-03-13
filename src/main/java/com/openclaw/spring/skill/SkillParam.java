package com.openclaw.spring.skill;

import java.lang.annotation.*;

/**
 * 标注方法参数为 Skill 动作的输入参数
 * 
 * 用法：
 * <pre>
 * public String greet(&#64;SkillParam(value = "name", required = true) String name)
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SkillParam {

    /** 参数名称 */
    String value();

    /** 是否必填 */
    boolean required() default true;

    /** 参数描述 */
    String description() default "";

    /** 默认值（当 required=false 时） */
    String defaultValue() default "";
}
