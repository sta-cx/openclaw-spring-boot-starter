package com.openclaw.spring.mcp;

import com.openclaw.spring.model.OpenClawTool;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillAction;
import com.openclaw.spring.skill.SkillParam;
import com.openclaw.spring.skill.SkillRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class McpToolAdapterTest {

    @OpenClawSkill(name = "demo", description = "Demo skill")
    static class DemoSkill {
        @SkillAction(name = "greet", description = "Say hello")
        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    @Test
    void testToMcpTools() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new DemoSkill());
        McpToolAdapter adapter = new McpToolAdapter(registry);

        List<OpenClawTool> tools = adapter.toMcpTools();
        assertEquals(1, tools.size());
        assertEquals("demo__greet", tools.get(0).getName());
        assertNotNull(tools.get(0).getInputSchema());
    }

    @Test
    void testExecuteToolCall() throws Exception {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new DemoSkill());
        McpToolAdapter adapter = new McpToolAdapter(registry);

        Object result = adapter.executeToolCall("demo__greet", Map.of("name", "JARVIS"));
        assertEquals("Hello, JARVIS", result);
    }

    @Test
    void testInvalidToolName() {
        SkillRegistry registry = new SkillRegistry();
        McpToolAdapter adapter = new McpToolAdapter(registry);

        assertThrows(IllegalArgumentException.class, () ->
            adapter.executeToolCall("invalid", null));
    }
}
