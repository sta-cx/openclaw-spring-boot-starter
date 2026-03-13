package com.openclaw.spring.mcp;

import com.openclaw.spring.model.OpenClawTool;
import com.openclaw.spring.skill.SkillRegistry;

import java.util.*;

/**
 * MCP Tool 适配器
 * 
 * 将 OpenClaw Skill 转换为 MCP 兼容的 Tool 定义
 * 参考 MCP Java SDK 的 Tool 规范设计
 */
public class McpToolAdapter {

    private final SkillRegistry skillRegistry;

    public McpToolAdapter(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    /**
     * 将所有已注册的 Skill 转换为 MCP Tool 列表
     */
    public List<OpenClawTool> toMcpTools() {
        List<OpenClawTool> tools = new ArrayList<>();

        for (Map.Entry<String, SkillRegistry.RegisteredSkill> entry : skillRegistry.getSkills().entrySet()) {
            SkillRegistry.RegisteredSkill skill = entry.getValue();

            for (Map.Entry<String, SkillRegistry.SkillActionInfo> actionEntry : skill.getActions().entrySet()) {
                SkillRegistry.SkillActionInfo action = actionEntry.getValue();

                // 生成 tool name: skillName__actionName
                String toolName = skill.getName() + "__" + action.getName();

                // 构建 input schema
                Map<String, Object> schema = new LinkedHashMap<>();
                schema.put("type", "object");

                Map<String, Object> properties = new LinkedHashMap<>();
                properties.put("input", OpenClawTool.prop("string", action.getDescription()));
                schema.put("properties", properties);

                OpenClawTool tool = new OpenClawTool(toolName, action.getDescription(), schema);
                tools.add(tool);
            }
        }

        return tools;
    }

    /**
     * 执行 MCP 格式的 tool call
     */
    public Object executeToolCall(String toolName, Map<String, Object> params) throws Exception {
        // 解析 toolName: skillName__actionName
        String[] parts = toolName.split("__", 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid tool name format: " + toolName);
        }

        String skillName = parts[0];
        String actionName = parts[1];

        return skillRegistry.execute(skillName, actionName, params);
    }
}
