package com.openclaw.spring.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SkillInfoTest {

    @Test
    void testSetters() {
        SkillInfo skill = new SkillInfo();
        skill.setName("weather");
        skill.setDescription("Weather forecast");
        skill.setVersion("1.0.0");
        skill.setAuthor("sta-cx");

        assertEquals("weather", skill.getName());
        assertEquals("Weather forecast", skill.getDescription());
        assertEquals("1.0.0", skill.getVersion());
        assertEquals("sta-cx", skill.getAuthor());
    }
}
