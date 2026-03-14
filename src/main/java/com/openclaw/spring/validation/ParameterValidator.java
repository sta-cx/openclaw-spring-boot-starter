package com.openclaw.spring.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openclaw.spring.skill.SkillParam;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Skill 参数验证器
 * 
 * 支持：
 * 1. @SkillParam 注解的 required 校验
 * 2. JSON Schema 基础校验（type, required, properties）
 */
public class ParameterValidator {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 验证方法调用参数
     * 
     * @param method 要调用的方法
     * @param params 传入的参数 Map
     * @return 验证结果（空列表 = 通过）
     */
    public static List<ValidationError> validate(Method method, Map<String, Object> params) {
        List<ValidationError> errors = new ArrayList<>();
        if (params == null) {
            params = Collections.emptyMap();
        }

        // 1. 检查 @SkillParam 的 required 约束
        for (Parameter param : method.getParameters()) {
            SkillParam skillParam = param.getAnnotation(SkillParam.class);
            if (skillParam != null && skillParam.required()) {
                String paramName = skillParam.value();
                if (!params.containsKey(paramName) || params.get(paramName) == null) {
                    errors.add(new ValidationError(paramName, "REQUIRED_MISSING",
                            "Required parameter missing: " + paramName));
                }
            }
        }

        // 2. 如果有 JSON Schema，做 schema 校验
        // Schema validation is handled separately via validateSchema()

        return errors;
    }

    /**
     * 使用 JSON Schema 验证参数
     * 
     * @param schemaJson JSON Schema 字符串
     * @param params     要验证的参数
     * @return 验证结果
     */
    public static List<ValidationError> validateSchema(String schemaJson, Map<String, Object> params) {
        List<ValidationError> errors = new ArrayList<>();
        if (schemaJson == null || schemaJson.isBlank()) {
            return errors;
        }

        try {
            JsonNode schema = objectMapper.readTree(schemaJson);
            if (params == null) {
                params = Collections.emptyMap();
            }

            // 检查 required 字段
            JsonNode requiredNode = schema.get("required");
            if (requiredNode != null && requiredNode.isArray()) {
                for (JsonNode req : requiredNode) {
                    String fieldName = req.asText();
                    if (!params.containsKey(fieldName)) {
                        errors.add(new ValidationError(fieldName, "SCHEMA_REQUIRED",
                                "Schema requires field: " + fieldName));
                    }
                }
            }

            // 检查 properties 的 type 约束
            JsonNode properties = schema.get("properties");
            if (properties != null && properties.isObject()) {
                Iterator<Map.Entry<String, JsonNode>> fields = properties.fields();
                while (fields.hasNext()) {
                    Map.Entry<String, JsonNode> entry = fields.next();
                    String fieldName = entry.getKey();
                    JsonNode fieldSchema = entry.getValue();

                    if (params.containsKey(fieldName)) {
                        Object value = params.get(fieldName);
                        String expectedType = fieldSchema.has("type") ? fieldSchema.get("type").asText() : null;
                        if (expectedType != null) {
                            String error = checkType(fieldName, value, expectedType);
                            if (error != null) {
                                errors.add(new ValidationError(fieldName, "SCHEMA_TYPE_MISMATCH", error));
                            }
                        }

                        // 检查 enum 约束
                        JsonNode enumNode = fieldSchema.get("enum");
                        if (enumNode != null && enumNode.isArray() && value != null) {
                            boolean found = false;
                            for (JsonNode enumVal : enumNode) {
                                if (enumVal.asText().equals(String.valueOf(value))) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                errors.add(new ValidationError(fieldName, "SCHEMA_ENUM_MISMATCH",
                                        String.format("Field '%s' must be one of %s, got: %s",
                                                fieldName, enumNode.toString(), value)));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            errors.add(new ValidationError("_schema", "SCHEMA_PARSE_ERROR",
                    "Failed to parse schema: " + e.getMessage()));
        }

        return errors;
    }

    private static String checkType(String fieldName, Object value, String expectedType) {
        if (value == null) return null;

        boolean match = switch (expectedType) {
            case "string" -> value instanceof String;
            case "number" -> value instanceof Number;
            case "integer" -> value instanceof Integer || value instanceof Long;
            case "boolean" -> value instanceof Boolean;
            case "array" -> value instanceof List || value instanceof Object[];
            case "object" -> value instanceof Map;
            default -> true;
        };

        if (!match) {
            return String.format("Field '%s' expected type '%s' but got '%s'",
                    fieldName, expectedType, value.getClass().getSimpleName());
        }
        return null;
    }

    /**
     * 验证错误详情
     */
    public static class ValidationError {
        private final String field;
        private final String code;
        private final String message;

        public ValidationError(String field, String code, String message) {
            this.field = field;
            this.code = code;
            this.message = message;
        }

        public String getField() { return field; }
        public String getCode() { return code; }
        public String getMessage() { return message; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", code, field, message);
        }
    }
}
