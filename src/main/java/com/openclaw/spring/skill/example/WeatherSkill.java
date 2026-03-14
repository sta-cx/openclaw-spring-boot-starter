package com.openclaw.spring.skill.example;

import com.openclaw.spring.event.EventTypes;
import com.openclaw.spring.event.OpenClawEvent;
import com.openclaw.spring.event.OpenClawEventListener;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillAction;
import com.openclaw.spring.skill.SkillParam;

import java.util.HashMap;
import java.util.Map;

/**
 * 示例 Skill：天气查询
 * 
 * 演示 @OpenClawSkill + @SkillAction + @OpenClawEventListener 的完整用法
 * 实际项目中应替换为真实天气 API
 */
@OpenClawSkill(name = "weather", description = "天气查询服务", version = "1.0.0")
public class WeatherSkill {

    // 模拟天气数据
    private static final Map<String, String> WEATHER_DATA = new HashMap<>();
    static {
        WEATHER_DATA.put("北京", "晴转多云，气温 5°C~18°C，东南风 3 级");
        WEATHER_DATA.put("上海", "多云，气温 12°C~22°C，东风 2 级");
        WEATHER_DATA.put("广州", "小雨，气温 20°C~28°C，南风 3 级");
        WEATHER_DATA.put("深圳", "阴天，气温 21°C~27°C，微风");
        WEATHER_DATA.put("杭州", "晴天，气温 10°C~20°C，西北风 2 级");
    }

    /**
     * 查询指定城市天气
     */
    @SkillAction(name = "query", description = "查询城市天气")
    public String query(@SkillParam("city") String city) {
        String weather = WEATHER_DATA.getOrDefault(city, 
            "暂无" + city + "的天气数据，仅支持：北京、上海、广州、深圳、杭州");
        return "🌤️ " + city + "天气：" + weather;
    }

    /**
     * 查询所有城市天气
     */
    @SkillAction(name = "all", description = "查询所有城市天气")
    public String queryAll() {
        StringBuilder sb = new StringBuilder("🌤️ 全国主要城市天气：\n");
        WEATHER_DATA.forEach((city, weather) -> 
            sb.append("• ").append(city).append("：").append(weather).append("\n"));
        return sb.toString();
    }

    /**
     * 监听消息事件 — 记录被查询的关键词
     */
    @OpenClawEventListener(EventTypes.MESSAGE_RECEIVED)
    public void onMessageReceived(OpenClawEvent event) {
        // 实际项目中可以做日志记录、统计等
        // System.out.println("[WeatherSkill] Message received: " + event.getData());
    }

    /**
     * 监听 Skill 执行完成事件
     */
    @OpenClawEventListener(EventTypes.SKILL_EXECUTE_COMPLETE)
    public void onSkillExecuted(OpenClawEvent event) {
        // 可以做执行统计、监控等
    }
}
