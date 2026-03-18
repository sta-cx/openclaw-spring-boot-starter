# OpenClaw Spring Boot Starter — 30天开发计划

> 起始日期：2026-03-18
> 结束日期：2026-04-16
> 目标：从 v0.1.0-SNAPSHOT 迈向 v1.0.0-RC1

---

## 📋 总览

| 阶段 | 天数 | 主题 | 目标版本 |
|------|------|------|----------|
| Phase 1 | Day 1-5 | 测试加固与代码质量 | v0.2.0 |
| Phase 2 | Day 6-10 | 流式响应与错误处理 | v0.3.0 |
| Phase 3 | Day 11-15 | Spring AI 集成 | v0.4.0 |
| Phase 4 | Day 16-20 | 通道适配器（QQ/微信/Telegram） | v0.5.0 |
| Phase 5 | Day 21-25 | 性能优化与监控 | v0.6.0 |
| Phase 6 | Day 26-30 | 文档、示例与发布准备 | v1.0.0-RC1 |

---

## 🗓️ 详细计划

### Phase 1：测试加固与代码质量（Day 1-5）

#### Day 1 — 补齐单元测试（核心模块）
- [ ] `OpenClawClientTest` — 覆盖所有 Gateway API 调用
- [ ] `SkillRegistryTest` — 注册/注销/查询边界测试
- [ ] `ParameterValidatorTest` — required/默认值/类型校验

#### Day 2 — 补齐单元测试（自动配置）
- [ ] 每个 AutoConfiguration 类的条件装配测试
- [ ] `OpenClawPropertiesTest` — 配置绑定与默认值
- [ ] `OpenClawHealthIndicatorTest` — UP/DOWN/UNKNOWN 状态

#### Day 3 — 集成测试完善
- [ ] `OpenClawStarterIntegrationTest` — 端到端 Skill 执行
- [ ] REST API 测试 — `/api/openclaw/skills` 全方法
- [ ] WebSocket 集成测试（嵌入式 Gateway mock）

#### Day 4 — 静态分析与代码规范
- [ ] 添加 SpotBugs 或 Error Prone 静态分析
- [ ] 添加 Checkstyle 规则
- [ ] 修复所有静态分析问题

#### Day 5 — CI 增强 + 发布 v0.2.0
- [ ] GitHub Actions: 添加测试覆盖率报告（JaCoCo）
- [ ] GitHub Actions: 添加静态分析步骤
- [ ] README 更新测试覆盖率徽章
- [ ] 版本号 bump → v0.2.0，打 tag，push

---

### Phase 2：流式响应与错误处理（Day 6-10）

#### Day 6 — SSE 流式响应支持
- [ ] `OpenClawClient` 添加 `streamChat()` 方法
- [ ] 支持 Server-Sent Events (SSE) 解析
- [ ] `StreamingResponse` 模型类

#### Day 7 — 错误处理体系
- [ ] `OpenClawException` 层次结构
- [ ] `GatewayConnectionException` — 连接失败
- [ ] `SkillExecutionException` — 技能执行失败
- [ ] `AuthenticationException` — 认证失败

#### Day 8 — 重试机制
- [ ] 可配置的重试策略（max-attempts, backoff）
- [ ] 指数退避 + 抖动
- [ ] 可重试异常标记（连接超时、5xx）
- [ ] 配置项：`openclaw.gateway.retry.*`

#### Day 9 — 连接池与超时配置
- [ ] WebClient 连接池配置（max-connections, max-idle-time）
- [ ] 读取超时、写入超时、连接超时分离配置
- [ ] 连接池 metrics 暴露到 Actuator

#### Day 10 — 测试 + 发布 v0.3.0
- [ ] SSE 流式响应测试
- [ ] 重试机制测试（模拟失败场景）
- [ ] 错误处理测试
- [ ] 版本号 bump → v0.3.0，打 tag，push

---

### Phase 3：Spring AI 集成（Day 11-15）

#### Day 11 — Spring AI 依赖与基础
- [ ] 添加 `spring-ai-core` 可选依赖
- [ ] `OpenClawChatModel` — 实现 Spring AI `ChatModel` 接口
- [ ] 基本的 prompt → response 映射

#### Day 12 — ChatClient 适配
- [ ] `OpenClawChatClient` — 实现 Spring AI `ChatClient`
- [ ] 支持 system prompt
- [ ] 支持 conversation history

#### Day 13 — Tool/Function Calling 适配
- [ ] Spring AI `ToolDefinition` → OpenClaw `SkillAction` 映射
- [ ] `@Tool` 注解自动注册为 Skill
- [ ] Tool 执行结果回传

#### Day 14 — Spring AI Auto-Configuration
- [ ] `SpringAiAutoConfiguration` — 条件装配
- [ ] `spring-ai-starter-openclaw` 模块结构
- [ ] 配置项：`spring.ai.openclaw.*`

#### Day 15 — 测试 + 发布 v0.4.0
- [ ] ChatModel 测试（mock Gateway）
- [ ] Tool Calling 端到端测试
- [ ] README 添加 Spring AI 集成章节
- [ ] 版本号 bump → v0.4.0，打 tag，push

---

### Phase 4：通道适配器（Day 16-20）

#### Day 16 — 适配器抽象层
- [ ] `ChannelAdapter` 接口定义
- [ ] `ChannelMessage` 模型
- [ ] `ChannelAdapterFactory` 工厂类
- [ ] 消息格式统一转换

#### Day 17 — QQ Bot 适配器
- [ ] `QQBotChannelAdapter` 实现
- [ ] QQ 消息接收/发送
- [ ] QQ 富文本支持（图片、语音）
- [ ] QQ 事件处理（加群、退群、@消息）

#### Day 18 — 企业微信适配器
- [ ] `WeWorkChannelAdapter` 实现
- [ ] 企业微信消息加解密
- [ ] 消息回调处理
- [ ] 应用菜单与事件

#### Day 19 — Telegram 适配器
- [ ] `TelegramChannelAdapter` 实现
- [ ] Telegram Bot API 集成
- [ ] Webhook 和 Long Polling 两种模式
- [ ] Inline Keyboard 支持

#### Day 20 — 测试 + 发布 v0.5.0
- [ ] 各通道适配器单元测试
- [ ] 消息格式转换测试
- [ ] README 添加通道适配器章节
- [ ] 版本号 bump → v0.5.0，打 tag，push

---

### Phase 5：性能优化与监控（Day 21-25）

#### Day 21 — 响应缓存
- [ ] Skill 执行结果缓存（可配置 TTL）
- [ ] `@Cacheable` 支持
- [ ] Caffeine 缓存集成

#### Day 22 — 指标增强
- [ ] Micrometer 指标细化
  - Skill 调用次数/耗时/错误率
  - Gateway 请求延迟分布
  - 活跃 Session 数
- [ ] Prometheus 指标端点

#### Day 23 — 健康检查增强
- [ ] Gateway 连接健康（含延迟）
- [ ] Skill 注册状态健康
- [ ] 自定义健康检查扩展点

#### Day 24 — 安全增强
- [ ] JWT 认证支持
- [ ] RBAC 权限控制（Skill 级别）
- [ ] 请求签名验证
- [ ] CORS 配置

#### Day 25 — 测试 + 发布 v0.6.0
- [ ] 缓存测试
- [ ] 安全测试
- [ ] 性能基准测试
- [ ] 版本号 bump → v0.6.0，打 tag，push

---

### Phase 6：文档、示例与发布准备（Day 26-30）

#### Day 26 — 示例应用
- [ ] `samples/weather-bot` — 天气查询 Bot 示例
- [ ] `samples/ai-assistant` — AI 助手示例（含 Spring AI）
- [ ] `samples/qq-bot` — QQ Bot 示例
- [ ] 每个示例含 README 和 docker-compose

#### Day 27 — API 文档
- [ ] Javadoc 完善（所有 public API）
- [ ] Swagger/OpenAPI 文档生成
- [ ] 配置参考文档（所有配置项）

#### Day 28 — 用户指南
- [ ] Getting Started 指南
- [ ] Migration Guide（从其他框架迁移）
- [ ] FAQ 文档
- [ ] Troubleshooting 指南

#### Day 29 — 发布准备
- [ ] Maven Central 发布流程验证
- [ ] GPG 签名测试
- [ ] 版本号正式化（去掉 SNAPSHOT）
- [ ] CHANGELOG.md 编写
- [ ] Release Notes 准备

#### Day 30 — 发布 v1.0.0-RC1
- [ ] 最终回归测试
- [ ] 所有文档最终审核
- [ ] GitHub Release 创建
- [ ] 版本号 bump → v1.0.0-RC1，打 tag，push
- [ ] 发布公告

---

## 📊 每日工作流

```
1. 晨间：检视今日计划，拉取最新代码
2. 开发：按计划实现功能
3. 测试：确保所有测试通过
4. 文档：更新相关文档
5. 提交：git add → git commit → git push
6. 汇报：通知先生提交详情
```

## 🔧 技术债务追踪

- [ ] 代码重复检查
- [ ] 依赖版本更新
- [ ] JavaDoc 覆盖率提升
- [ ] 异常信息国际化（i18n）

---

> 本计划将每日更新实际进度。
> 每日提交前运行 `mvn clean test` 确保质量。
