package com.openclaw.spring.model;

import java.util.Map;

/**
 * OpenClaw Tool 定义 - 对标 MCP Tool 规范
 * 
 * 基于 MCP Java SDK 的 Tool 设计，适配 OpenClaw Gateway
 */
public class OpenClawTool {

    private String name;
    private String title;
    private String description;
    private Map<String, Object> inputSchema;  // JSON Schema
    private Map<String, Object> outputSchema;
    private Map<String, Object> annotations;

    public OpenClawTool() {}

    public OpenClawTool(String name, String description, Map<String, Object> inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }

    // Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Map<String, Object> getInputSchema() { return inputSchema; }
    public void setInputSchema(Map<String, Object> inputSchema) { this.inputSchema = inputSchema; }
    public Map<String, Object> getOutputSchema() { return outputSchema; }
    public void setOutputSchema(Map<String, Object> outputSchema) { this.outputSchema = outputSchema; }
    public Map<String, Object> getAnnotations() { return annotations; }
    public void setAnnotations(Map<String, Object> annotations) { this.annotations = annotations; }

    /**
     * 构建 JSON Schema 的便捷方法
     */
    public static Map<String, Object> schema(String type, Map<String, Object> properties, String... required) {
        Map<String, Object> schema = new java.util.LinkedHashMap<>();
        schema.put("type", type);
        if (properties != null) schema.put("properties", properties);
        if (required != null && required.length > 0) schema.put("required", java.util.List.of(required));
        return schema;
    }

    /**
     * 构建属性定义的便捷方法
     */
    public static Map<String, Object> prop(String type, String description) {
        Map<String, Object> prop = new java.util.LinkedHashMap<>();
        prop.put("type", type);
        if (description != null) prop.put("description", description);
        return prop;
    }
}
