package com.openclaw.spring.event;

import java.lang.annotation.*;

/**
 * 标记事件监听方法
 * 用在 @OpenClawSkill 或任何 Spring Bean 的方法上
 * 
 * 示例:
 * <pre>
 * {@literal @}OpenClawSkill(name = "logger", description = "日志记录")
 * public class LoggerSkill {
 *     {@literal @}OpenClawEventListener(EventTypes.MESSAGE_RECEIVED)
 *     public void onMessage(OpenClawEvent event) {
 *         // 处理消息事件
 *     }
 * }
 * </pre>
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface OpenClawEventListener {

    /**
     * 监听的事件类型（支持通配符 "*" 监听所有事件）
     */
    String[] value();

    /**
     * 优先级，数字越小越先执行
     */
    int order() default 0;
}
