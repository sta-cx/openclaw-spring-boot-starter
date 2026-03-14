# OpenClaw Spring Boot Starter

Spring Boot 集成 OpenClaw AI Agent 的官方 Starter。

## 功能特性

| 模块 | 说明 |
|------|------|
| **Gateway Client** | 自动配置 OpenClaw Gateway 的 HTTP 客户端 |
| **Skill 框架** | 基于注解的 Skill 开发（`@OpenClawSkill` / `@SkillAction` / `@SkillParam`） |
| **MCP 适配** | 自动将 Skill 转换为 MCP Tool 兼容格式 |
| **事件系统** | 异步事件发布与监听（`@OpenClawEventListener`） |
| **REST API** | Skill 管理 REST 接口（查询 / 执行 / 参数验证） |
| **参数验证** | `@SkillParam` required 校验 + JSON Schema 基础校验 |
| **Actuator** | Spring Boot Health Indicator（Gateway 连接状态） |

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.openclaw</groupId>
    <artifactId>openclaw-spring-boot-starter</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

### 2. 配置 Gateway 连接

```yaml
openclaw:
  gateway:
    url: http://localhost:18789
    token: your-api-token    # 可选
    timeout: 30              # 可选，默认 30 秒
```

### 3. 编写 Skill

```java
@OpenClawSkill(name = "weather", description = "天气查询", version = "1.0.0")
public class WeatherSkill {

    @SkillAction(name = "query", description = "查询城市天气")
    public String query(@SkillParam(value = "city", description = "城市名") String city) {
        return "Beijing: 22°C, Sunny";
    }
}
```

将 Skill 注册为 Spring Bean 即可自动发现：

```java
@Configuration
public class SkillConfig {
    @Bean
    public WeatherSkill weatherSkill() {
        return new WeatherSkill();
    }
}
```

### 4. 事件监听

```java
@OpenClawEventListener("user.message")
public void onMessage(OpenClawEvent event) {
    System.out.println("Received: " + event.getData());
}
```

## REST API

启动后自动暴露以下接口：

| 方法 | 路径 | 说明 |
|------|------|------|
| `GET` | `/api/openclaw/skills` | 列出所有已注册 Skill |
| `GET` | `/api/openclaw/skills/{name}` | 获取 Skill 详情（含 action 列表） |
| `POST` | `/api/openclaw/skills/{name}/execute` | 执行 Skill 动作 |

### 执行 Skill 示例

```bash
curl -X POST http://localhost:8080/api/openclaw/skills/weather/execute \
  -H "Content-Type: application/json" \
  -d '{"action": "query", "params": {"city": "Beijing"}}'
```

响应：

```json
{
  "success": true,
  "skill": "weather",
  "action": "query",
  "result": "Beijing: 22°C, Sunny"
}
```

## 参数验证

### @SkillParam 验证

```java
public String greet(
    @SkillParam(value = "name", required = true) String name,
    @SkillParam(value = "lang", required = false, defaultValue = "en") String lang
)
```

缺少必填参数时返回：

```json
{
  "error": "VALIDATION_FAILED",
  "errors": [
    {"field": "name", "code": "REQUIRED_MISSING", "message": "Required parameter missing: name"}
  ]
}
```

### JSON Schema 验证

```java
@SkillAction(name = "convert", schema = """
    {
      "required": ["value", "unit"],
      "properties": {
        "value": {"type": "number"},
        "unit": {"type": "string", "enum": ["celsius", "fahrenheit"]}
      }
    }
    """)
public String convert(@SkillParam("value") double value, @SkillParam("unit") String unit) { ... }
```

## Actuator Health

添加 `spring-boot-starter-actuator` 依赖后，`/actuator/health` 自动包含 OpenClaw Gateway 状态：

```json
{
  "status": "UP",
  "components": {
    "openClaw": {
      "status": "UP",
      "details": {
        "gateway": "connected"
      }
    }
  }
}
```

## 客户端 API

注入 `OpenClawClient` 直接调用 Gateway：

```java
@Autowired
private OpenClawClient client;

// 发送消息
client.sendMessage("Hello").subscribe(response -> {
    System.out.println(response);
});

// 列出技能
client.listSkills().subscribe(skills -> {
    System.out.println(skills);
});
```

## 配置项

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `openclaw.gateway.url` | `http://localhost:18789` | Gateway 地址 |
| `openclaw.gateway.token` | - | API 认证 Token |
| `openclaw.gateway.timeout` | `30` | 超时时间（秒） |

## 许可证

MIT License
