package com.openclaw.spring.model;

/**
 * OpenClaw 会话模型
 */
public class Session {

    private String id;
    private String label;
    private String createdAt;
    private String lastActiveAt;

    public Session() {}

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getLastActiveAt() { return lastActiveAt; }
    public void setLastActiveAt(String lastActiveAt) { this.lastActiveAt = lastActiveAt; }
}
