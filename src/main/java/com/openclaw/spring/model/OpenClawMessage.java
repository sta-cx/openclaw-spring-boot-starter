package com.openclaw.spring.model;

/**
 * OpenClaw 消息模型
 */
public class OpenClawMessage {

    private String id;
    private String role;  // "user" | "assistant" | "system"
    private String content;
    private long timestamp;

    public OpenClawMessage() {}

    public OpenClawMessage(String role, String content) {
        this.role = role;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public static OpenClawMessage user(String content) {
        return new OpenClawMessage("user", content);
    }

    public static OpenClawMessage assistant(String content) {
        return new OpenClawMessage("assistant", content);
    }
}
