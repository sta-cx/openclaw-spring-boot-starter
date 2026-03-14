package com.openclaw.spring.web;

import com.openclaw.spring.skill.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SkillControllerTest {

    private SkillRegistry registry;
    private SkillController controller;

    @OpenClawSkill(name = "math", description = "Math operations", version = "1.0.0")
    static class MathSkill {
        @SkillAction(name = "add", description = "Add two numbers")
        public int add(@SkillParam(value = "a") int a, @SkillParam(value = "b") int b) {
            return a + b;
        }

        @SkillAction(name = "greet", description = "Say hello")
        public String greet(@SkillParam(value = "name") String name) {
            return "Hello, " + name + "!";
        }
    }

    @BeforeEach
    void setup() {
        registry = new SkillRegistry();
        registry.register(new MathSkill());
        controller = new SkillController(registry);
    }

    @Test
    void shouldListSkills() {
        var response = controller.listSkills();
        assertTrue(response.getStatusCode().is2xxSuccessful());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(1, body.get("total"));

        @SuppressWarnings("unchecked")
        var skills = (java.util.List<Map<String, String>>) body.get("skills");
        assertEquals("math", skills.get(0).get("name"));
    }

    @Test
    void shouldGetSkillDetail() {
        var response = controller.getSkill("math");
        assertTrue(response.getStatusCode().is2xxSuccessful());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("math", body.get("name"));
        assertEquals("1.0.0", body.get("version"));

        @SuppressWarnings("unchecked")
        var actions = (java.util.List<Map<String, String>>) body.get("actions");
        assertEquals(2, actions.size());
    }

    @Test
    void shouldReturn404ForUnknownSkill() {
        var response = controller.getSkill("nonexistent");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void shouldExecuteSkill() {
        var request = Map.<String, Object>of(
                "action", "greet",
                "params", Map.of("name", "World")
        );
        var response = controller.executeSkill("math", request);
        assertTrue(response.getStatusCode().is2xxSuccessful());

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(true, body.get("success"));
        assertEquals("Hello, World!", body.get("result"));
    }

    @Test
    void shouldReturn400WhenActionMissing() {
        var request = Map.<String, Object>of("params", Map.of());
        var response = controller.executeSkill("math", request);
        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void shouldReturn404WhenActionNotFound() {
        var request = Map.<String, Object>of("action", "nonexistent", "params", Map.of());
        var response = controller.executeSkill("math", request);
        assertEquals(404, response.getStatusCode().value());
    }
}
