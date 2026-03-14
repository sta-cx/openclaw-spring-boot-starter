package com.openclaw.spring.validation;

import com.openclaw.spring.skill.SkillParam;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ParameterValidatorTest {

    // 测试用方法
    public String testMethod(
            @SkillParam(value = "city", required = true) String city,
            @SkillParam(value = "unit", required = false, defaultValue = "celsius") String unit) {
        return city + ":" + unit;
    }

    @Test
    void shouldPassWhenRequiredParamPresent() throws Exception {
        Method method = this.getClass().getDeclaredMethod("testMethod", String.class, String.class);
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validate(method,
                Map.of("city", "Beijing"));
        assertTrue(errors.isEmpty(), "Should pass with required param present");
    }

    @Test
    void shouldFailWhenRequiredParamMissing() throws Exception {
        Method method = this.getClass().getDeclaredMethod("testMethod", String.class, String.class);
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validate(method,
                Map.of("unit", "fahrenheit"));
        assertFalse(errors.isEmpty(), "Should fail when required param missing");
        assertEquals("REQUIRED_MISSING", errors.get(0).getCode());
        assertEquals("city", errors.get(0).getField());
    }

    @Test
    void shouldPassWithNullParams() throws Exception {
        Method method = this.getClass().getDeclaredMethod("testMethod", String.class, String.class);
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validate(method, null);
        assertFalse(errors.isEmpty(), "Should fail for required params when null");
    }

    // JSON Schema 验证测试

    @Test
    void shouldValidateSchemaRequiredFields() {
        String schema = """
                {
                    "type": "object",
                    "required": ["name", "age"],
                    "properties": {
                        "name": {"type": "string"},
                        "age": {"type": "integer"}
                    }
                }
                """;

        // 缺少 age
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validateSchema(schema,
                Map.of("name", "test"));
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> "age".equals(e.getField())));
    }

    @Test
    void shouldValidateSchemaTypes() {
        String schema = """
                {
                    "type": "object",
                    "properties": {
                        "count": {"type": "integer"},
                        "name": {"type": "string"}
                    }
                }
                """;

        // 类型错误：count 应为 integer 但传了 string
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validateSchema(schema,
                Map.of("count", "not-a-number", "name", "hello"));
        assertFalse(errors.isEmpty());
        assertTrue(errors.stream().anyMatch(e -> "count".equals(e.getField()) && "SCHEMA_TYPE_MISMATCH".equals(e.getCode())));
    }

    @Test
    void shouldValidateSchemaEnum() {
        String schema = """
                {
                    "properties": {
                        "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
                    }
                }
                """;

        // 无效的 enum 值
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validateSchema(schema,
                Map.of("unit", "kelvin"));
        assertFalse(errors.isEmpty());
        assertEquals("SCHEMA_ENUM_MISMATCH", errors.get(0).getCode());

        // 有效的 enum 值
        errors = ParameterValidator.validateSchema(schema, Map.of("unit", "celsius"));
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldPassWithEmptySchema() {
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validateSchema("", Map.of());
        assertTrue(errors.isEmpty());
    }

    @Test
    void shouldHandleInvalidSchema() {
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validateSchema("not-json", Map.of());
        assertFalse(errors.isEmpty());
        assertEquals("SCHEMA_PARSE_ERROR", errors.get(0).getCode());
    }
}
