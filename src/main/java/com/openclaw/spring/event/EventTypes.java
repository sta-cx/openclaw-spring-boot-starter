package com.openclaw.spring.event;

/**
 * 内置事件类型常量
 */
public final class EventTypes {

    private EventTypes() {}

    /** 消息收到 */
    public static final String MESSAGE_RECEIVED = "message.received";

    /** 消息发送 */
    public static final String MESSAGE_SENT = "message.sent";

    /** 会话创建 */
    public static final String SESSION_CREATED = "session.created";

    /** 会话关闭 */
    public static final String SESSION_CLOSED = "session.closed";

    /** Skill 注册完成 */
    public static final String SKILL_REGISTERED = "skill.registered";

    /** Skill 执行开始 */
    public static final String SKILL_EXECUTE_START = "skill.execute.start";

    /** Skill 执行完成 */
    public static final String SKILL_EXECUTE_COMPLETE = "skill.execute.complete";

    /** Skill 执行失败 */
    public static final String SKILL_EXECUTE_ERROR = "skill.execute.error";

    /** Gateway 连接成功 */
    public static final String GATEWAY_CONNECTED = "gateway.connected";

    /** Gateway 连接断开 */
    public static final String GATEWAY_DISCONNECTED = "gateway.disconnected";

    /** Gateway 重连中 */
    public static final String GATEWAY_RECONNECT = "gateway.reconnect";

    /** WebSocket 消息收到 */
    public static final String WEBSOCKET_MESSAGE_RECEIVED = "websocket.message.received";

    /** WebSocket 连接错误 */
    public static final String WEBSOCKET_ERROR = "websocket.error";

    /** 通配符 — 监听所有事件 */
    public static final String ALL = "*";
}
