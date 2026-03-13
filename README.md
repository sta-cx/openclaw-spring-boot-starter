# OpenClaw Spring Boot Starter

让 Spring Boot 应用轻松接入 [OpenClaw](https://github.com/openclaw/openclaw) AI Agent。

## 功能特性

- 🚀 **开箱即用** — 一行配置接入 OpenClaw Gateway
- 🤖 **Agent 交互** — 发送消息、接收回复、管理会话
- 🔧 **Skill 开发** — 用 Java 开发 OpenClaw Skill
- 📡 **多通道支持** — 对接 QQ、微信、Telegram 等通道
- 🔄 **事件监听** — 监听 Agent 事件并自定义处理

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.openclaw</groupId>
    <artifactId>openclaw-spring-boot-starter</artifactId>
    <version>0.1.0</version>
</dependency>
```

### 2. 配置

```yaml
openclaw:
  gateway:
    url: http://localhost:18789
    token: your-api-token  # 可选
```

### 3. 使用

```java
@RestController
public class ChatController {

    @Autowired
    private OpenClawClient openClawClient;

    @PostMapping("/chat")
    public Mono<String> chat(@RequestBody String message) {
        return openClawClient.sendMessage(message);
    }
}
```

## API 文档

### OpenClawClient

| 方法 | 说明 |
|------|------|
| `sendMessage(String message)` | 发送消息并获取回复 |
| `createSession()` | 创建新会话 |
| `listSkills()` | 列出已安装的技能 |
| `executeSkill(String skill, String action)` | 执行指定技能 |

### OpenClawProperties

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `openclaw.gateway.url` | `http://localhost:18789` | Gateway 地址 |
| `openclaw.gateway.token` | - | 认证令牌 |
| `openclaw.gateway.timeout` | `30s` | 请求超时 |

## 示例项目

参见 [examples/demo](examples/demo) 目录。

## 开发计划

- [ ] 基础 Gateway REST API 客户端
- [ ] Skill 开发框架
- [ ] 事件监听机制
- [ ] 多通道消息适配器
- [ ] Spring AI 集成

## License

MIT
