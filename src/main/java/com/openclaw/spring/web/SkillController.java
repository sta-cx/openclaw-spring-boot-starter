package com.openclaw.spring.web;

import com.openclaw.spring.skill.SkillRegistry;
import com.openclaw.spring.validation.ParameterValidator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Skill 管理 REST API
 * 
 * 提供：
 * - GET    /api/openclaw/skills          — 列出所有已注册的 Skill
 * - GET    /api/openclaw/skills/{name}   — 获取 Skill 详情（含 action 列表）
 * - POST   /api/openclaw/skills/{name}/execute — 执行 Skill 动作
 */
@RestController
@RequestMapping("/api/openclaw/skills")
public class SkillController {

    private final SkillRegistry skillRegistry;

    public SkillController(SkillRegistry skillRegistry) {
        this.skillRegistry = skillRegistry;
    }

    /**
     * 列出所有已注册的 Skill
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listSkills() {
        Map<String, Object> result = new LinkedHashMap<>();
        List<Map<String, String>> skillList = new ArrayList<>();

        for (var entry : skillRegistry.getSkills().entrySet()) {
            var skill = entry.getValue();
            Map<String, String> info = new LinkedHashMap<>();
            info.put("name", skill.getName());
            info.put("description", skill.getDescription());
            info.put("version", skill.getVersion());
            info.put("actions", String.valueOf(skill.getActions().size()));
            skillList.add(info);
        }

        result.put("skills", skillList);
        result.put("total", skillList.size());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取指定 Skill 详情
     */
    @GetMapping("/{name}")
    public ResponseEntity<Map<String, Object>> getSkill(@PathVariable String name) {
        SkillRegistry.RegisteredSkill skill = skillRegistry.getSkill(name);
        if (skill == null) {
            return ResponseEntity.notFound().build();
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("name", skill.getName());
        result.put("description", skill.getDescription());
        result.put("version", skill.getVersion());

        List<Map<String, String>> actionList = new ArrayList<>();
        for (var actionEntry : skill.getActions().entrySet()) {
            var action = actionEntry.getValue();
            Map<String, String> actionInfo = new LinkedHashMap<>();
            actionInfo.put("name", action.getName());
            actionInfo.put("description", action.getDescription());

            // 提取参数信息
            Method method = action.getMethod();
            List<String> paramNames = Arrays.stream(method.getParameters())
                    .map(p -> {
                        var sp = p.getAnnotation(com.openclaw.spring.skill.SkillParam.class);
                        if (sp != null) {
                            return sp.value() + (sp.required() ? "*" : "");
                        }
                        return p.getName();
                    })
                    .collect(Collectors.toList());
            actionInfo.put("params", String.join(", ", paramNames));
            actionList.add(actionInfo);
        }
        result.put("actions", actionList);

        return ResponseEntity.ok(result);
    }

    /**
     * 执行 Skill 动作
     * 
     * Body: { "action": "actionName", "params": { ... } }
     */
    @PostMapping("/{name}/execute")
    public ResponseEntity<Map<String, Object>> executeSkill(
            @PathVariable String name,
            @RequestBody Map<String, Object> request) {

        Map<String, Object> response = new LinkedHashMap<>();

        String actionName = (String) request.get("action");
        if (actionName == null) {
            response.put("error", "MISSING_ACTION");
            response.put("message", "Request body must contain 'action' field");
            return ResponseEntity.badRequest().body(response);
        }

        SkillRegistry.RegisteredSkill skill = skillRegistry.getSkill(name);
        if (skill == null) {
            response.put("error", "SKILL_NOT_FOUND");
            response.put("message", "Skill not found: " + name);
            return ResponseEntity.status(404).body(response);
        }

        var actionInfo = skill.getActions().get(actionName);
        if (actionInfo == null) {
            response.put("error", "ACTION_NOT_FOUND");
            response.put("message", "Action not found: " + actionName + " in skill: " + name);
            return ResponseEntity.status(404).body(response);
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> params = (Map<String, Object>) request.getOrDefault("params", Collections.emptyMap());

        // 参数验证
        List<ParameterValidator.ValidationError> errors = ParameterValidator.validate(actionInfo.getMethod(), params);
        if (!errors.isEmpty()) {
            response.put("error", "VALIDATION_FAILED");
            response.put("errors", errors.stream()
                    .map(e -> Map.of("field", e.getField(), "code", e.getCode(), "message", e.getMessage()))
                    .toList());
            return ResponseEntity.badRequest().body(response);
        }

        try {
            Object result = skillRegistry.execute(name, actionName, params);
            response.put("success", true);
            response.put("skill", name);
            response.put("action", actionName);
            response.put("result", result);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "EXECUTION_FAILED");
            response.put("message", e.getMessage());
            Throwable cause = e.getCause();
            if (cause != null) {
                response.put("cause", cause.getMessage());
            }
            return ResponseEntity.status(500).body(response);
        }
    }
}
