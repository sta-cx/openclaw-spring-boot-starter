package com.openclaw.spring.skill;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class SkillRegistryTest {

    @OpenClawSkill(name = "test-skill", description = "A test skill", version = "0.1.0")
    static class TestSkill {
        @SkillAction(name = "greet", description = "Say hello")
        public String greet(String name) {
            return "Hello, " + name;
        }

        @SkillAction(name = "add")
        public int add(Map<String, Object> params) {
            return 1 + 1;
        }

        // This method should NOT be registered (no @SkillAction)
        public String helper() {
            return "helper";
        }
    }

    @Test
    void testRegisterSkill() {
        SkillRegistry registry = new SkillRegistry();
        TestSkill skill = new TestSkill();
        registry.register(skill);

        assertTrue(registry.hasSkill("test-skill"));
        assertNotNull(registry.getSkill("test-skill"));
    }

    @Test
    void testSkillMetadata() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new TestSkill());

        SkillRegistry.RegisteredSkill skill = registry.getSkill("test-skill");
        assertEquals("test-skill", skill.getName());
        assertEquals("A test skill", skill.getDescription());
        assertEquals("0.1.0", skill.getVersion());
    }

    @Test
    void testSkillActions() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new TestSkill());

        SkillRegistry.RegisteredSkill skill = registry.getSkill("test-skill");
        assertEquals(2, skill.getActions().size());
        assertTrue(skill.getActions().containsKey("greet"));
        assertTrue(skill.getActions().containsKey("add"));
        assertFalse(skill.getActions().containsKey("helper"));
    }

    @Test
    void testExecuteAction() throws Exception {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new TestSkill());

        Object result = registry.execute("test-skill", "greet", Map.of("name", "JARVIS"));
        assertEquals("Hello, JARVIS", result);
    }

    @Test
    void testSkillNotFound() {
        SkillRegistry registry = new SkillRegistry();
        assertFalse(registry.hasSkill("nonexistent"));
    }

    @Test
    void testExecuteNonexistentSkill() {
        SkillRegistry registry = new SkillRegistry();
        assertThrows(IllegalArgumentException.class, () -> 
            registry.execute("nonexistent", "action", null));
    }

    @Test
    void testGetAllSkills() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new TestSkill());

        Map<String, SkillRegistry.RegisteredSkill> skills = registry.getSkills();
        assertEquals(1, skills.size());
    }

    @Test
    void testUnannotatedClassThrows() {
        SkillRegistry registry = new SkillRegistry();
        assertThrows(IllegalArgumentException.class, () ->
            registry.register(new Object()));
    }

    @Test
    void testUnregisterSkill() {
        SkillRegistry registry = new SkillRegistry();
        registry.register(new TestSkill());
        assertTrue(registry.hasSkill("test-skill"));

        SkillRegistry.RegisteredSkill removed = registry.unregister("test-skill");
        assertNotNull(removed);
        assertEquals("test-skill", removed.getName());
        assertFalse(registry.hasSkill("test-skill"));
    }

    @Test
    void testUnregisterNonExistent() {
        SkillRegistry registry = new SkillRegistry();
        SkillRegistry.RegisteredSkill removed = registry.unregister("nonexistent");
        assertNull(removed);
    }

    @Test
    void testUnregisterByInstance() {
        SkillRegistry registry = new SkillRegistry();
        TestSkill skill = new TestSkill();
        registry.register(skill);
        assertTrue(registry.hasSkill("test-skill"));

        SkillRegistry.RegisteredSkill removed = registry.unregisterByInstance(skill);
        assertNotNull(removed);
        assertFalse(registry.hasSkill("test-skill"));
    }

    @Test
    void testUnregisterByWrongInstance() {
        SkillRegistry registry = new SkillRegistry();
        TestSkill skill1 = new TestSkill();
        TestSkill skill2 = new TestSkill();
        registry.register(skill1);

        // Trying to unregister with a different instance
        SkillRegistry.RegisteredSkill removed = registry.unregisterByInstance(skill2);
        assertNull(removed);
        assertTrue(registry.hasSkill("test-skill")); // Still registered
    }
}
