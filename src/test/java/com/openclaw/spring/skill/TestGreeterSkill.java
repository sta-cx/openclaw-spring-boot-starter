package com.openclaw.spring.skill;

/**
 * 测试用 Skill - 用于 Integration Test
 */
@OpenClawSkill(name = "greeter", description = "Test greeting skill", version = "0.0.1")
public class TestGreeterSkill {

    @SkillAction(name = "greet", description = "Say hello")
    public String greet(@SkillParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @SkillAction(name = "wave", description = "Wave goodbye")
    public String wave() {
        return "Goodbye!";
    }
}
