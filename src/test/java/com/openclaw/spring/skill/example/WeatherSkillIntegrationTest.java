package com.openclaw.spring.skill.example;

import com.openclaw.spring.event.EventListenerRegistry;
import com.openclaw.spring.event.EventTypes;
import com.openclaw.spring.event.OpenClawEvent;
import com.openclaw.spring.skill.SkillRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * WeatherSkill 集成测试
 * 验证 Skill 注册、执行、事件监听的完整流程
 */
class WeatherSkillIntegrationTest {

    private SkillRegistry skillRegistry;
    private EventListenerRegistry eventRegistry;
    private WeatherSkill weatherSkill;

    @BeforeEach
    void setUp() {
        skillRegistry = new SkillRegistry();
        eventRegistry = new EventListenerRegistry();
        weatherSkill = new WeatherSkill();

        // 注册 Skill
        skillRegistry.register(weatherSkill);
        // 注册事件监听器
        eventRegistry.registerListeners(weatherSkill);
    }

    @Test
    void testSkillRegistered() {
        assertTrue(skillRegistry.hasSkill("weather"));
        assertEquals("天气查询服务", skillRegistry.getSkill("weather").getDescription());
    }

    @Test
    void testQueryAction() throws Exception {
        Object result = skillRegistry.execute("weather", "query", Map.of("city", "北京"));
        assertNotNull(result);
        assertTrue(result.toString().contains("北京"));
        assertTrue(result.toString().contains("晴转多云"));
    }

    @Test
    void testQueryUnknownCity() throws Exception {
        Object result = skillRegistry.execute("weather", "query", Map.of("city", "火星"));
        assertTrue(result.toString().contains("暂无"));
    }

    @Test
    void testQueryAllAction() throws Exception {
        Object result = skillRegistry.execute("weather", "all", null);
        assertTrue(result.toString().contains("北京"));
        assertTrue(result.toString().contains("上海"));
        assertTrue(result.toString().contains("广州"));
    }

    @Test
    void testEventListenerRegistered() {
        assertTrue(eventRegistry.getEventTypes().contains(EventTypes.MESSAGE_RECEIVED));
        assertTrue(eventRegistry.getEventTypes().contains(EventTypes.SKILL_EXECUTE_COMPLETE));
    }

    @Test
    void testEventDispatch() {
        // 发布事件不应抛异常
        eventRegistry.publish(EventTypes.MESSAGE_RECEIVED, this, "test message");
        eventRegistry.publish(EventTypes.SKILL_EXECUTE_COMPLETE, this, "weather.query");
    }
}
