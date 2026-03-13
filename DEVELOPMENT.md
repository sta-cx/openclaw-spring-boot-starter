# OpenClaw Spring Boot Starter 开发文档

## 项目概述

**目标：** 让 Java/Spring Boot 开发者能够轻松接入 OpenClaw AI Agent 生态。

**定位：** OpenClaw 生态的 Java 入口，类似 Spring Boot Starter 模式，开箱即用。

**仓库：** https://github.com/sta-cx/openclaw-spring-boot-starter

---

## 核心功能模块

### 1. Gateway Client（Gateway 客户端）✅ 已完成基础版

与 OpenClaw Gateway REST API 通信的核心客户端。

**已实现：**
- `OpenClawClient` — 基于 Spring WebFlux 的响应式客户端
- 支持发送消息、创建会话、获取状态
- 支持 Bearer Token 认证

**待完善：**
- [ ] Session 管理（创建、销毁、列表）
- [ ] 消息历史查询
- [ ] 流式响应（SSE）
- [ ] 错误处理与重试机制
- [ ] 连接池配置

### 2. Skill Framework（技能框架）🔲 待开发

让 Java 开发者能够开发 OpenClaw Skill。

**设计思路：**
```java
@OpenClawSkill(name = "my-skill", description = "我的技能")
public class MySkill {
    
    @SkillAction("greet")
    public String greet(@SkillParam("name") String name) {
        return "Hello, " + name;
    }
}
```

**需要实现：**
- [ ] `@OpenClawSkill` 注解
- [ ] `@SkillAction` 注解
- [ ] `@SkillParam` 注解
- [ ] Skill 自动注册机制
- [ ] Skill 执行器

### 3. Event Listener（事件监听）🔲 待开发

监听 OpenClaw 事件并自定义处理。

**设计思路：**
```java
@Component
public class MyEventListener {
    
    @OnOpenClawEvent("message.received")
    public void onMessage(OpenClawEvent event) {
        // 处理消息
    }
}
```

**需要实现：**
- [ ] 事件模型定义
- [ ] `@OnOpenClawEvent` 注解
- [ ] 事件分发机制
- [ ] Webhook 接收端点

### 4. Channel Adapters（通道适配器）🔲 待开发

对接不同消息通道的适配器。

**目标通道：**
- [ ] QQ Bot
- [ ] 微信公众号/企业微信
- [ ] Telegram
- [ ] 钉钉
- [ ] 飞书

### 5. Spring AI Integration（Spring AI 集成）🔲 待开发

与 Spring AI 框架的深度集成。

**需要实现：**
- [ ] OpenClaw ChatClient
- [ ] OpenClaw ChatModel
- [ ] Tool/Function Calling 适配

---

## 技术架构

```
┌─────────────────────────────────────────┐
│         Spring Boot Application         │
├─────────────────────────────────────────┤
│   OpenClawAutoConfiguration             │
│   ├── OpenClawClient (WebFlux)          │
│   ├── OpenClawProperties                │
│   ├── SkillRegistry                     │
│   └── EventDispatcher                   │
├─────────────────────────────────────────┤
│   Spring Boot Actuator / Web            │
├─────────────────────────────────────────┤
│         OpenClaw Gateway API            │
│         (localhost:18789)               │
└─────────────────────────────────────────┘
```

---

## 配置参考

```yaml
openclaw:
  gateway:
    url: http://localhost:18789
    token: your-token
    timeout: 30
    # 连接池
    pool:
      max-connections: 10
      max-idle-time: 30s
    # 重试
    retry:
      max-attempts: 3
      backoff: 1s
```

---

## 开发路线图

### Phase 1: 基础功能（当前）
- [x] 项目结构搭建
- [x] 基础 Gateway Client
- [x] 配置属性
- [x] 自动配置
- [ ] 完善 Gateway API 调用
- [ ] 单元测试
- [ ] 发布 v0.1.0

### Phase 2: Skill 框架
- [ ] Skill 注解系统
- [ ] Skill 自动注册
- [ ] 示例 Skill
- [ ] 发布 v0.2.0

### Phase 3: 事件系统
- [ ] 事件模型
- [ ] 事件监听注解
- [ ] Webhook 端点
- [ ] 发布 v0.3.0

### Phase 4: 通道适配器
- [ ] QQ Bot 适配器
- [ ] 微信适配器
- [ ] Telegram 适配器
- [ ] 发布 v0.4.0

### Phase 5: Spring AI 集成
- [ ] ChatClient 实现
- [ ] Tool Calling 适配
- [ ] 发布 v1.0.0

---

## 参考资料

- [OpenClaw 文档](https://docs.openclaw.ai)
- [OpenClaw GitHub](https://github.com/openclaw/openclaw)
- [Spring AI Alibaba](https://github.com/alibaba/spring-ai-alibaba)
- [MCP Java SDK](https://github.com/modelcontextprotocol/java-sdk)
- [Spring Boot Starter 开发指南](https://docs.spring.io/spring-boot/reference/developing/auto-configuration.html)

---

## 开发环境

- Java 17+
- Maven 3.8+
- Spring Boot 3.4.3

## 构建

```bash
mvn clean install
```

## 测试

```bash
mvn test
```
