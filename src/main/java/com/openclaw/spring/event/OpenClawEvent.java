package com.openclaw.spring.event;

/**
 * OpenClaw 事件模型
 */
public class OpenClawEvent {

    private final String type;
    private final Object source;
    private final Object data;
    private final long timestamp;

    public OpenClawEvent(String type, Object source, Object data) {
        this.type = type;
        this.source = source;
        this.data = data;
        this.timestamp = System.currentTimeMillis();
    }

    public String getType() { return type; }
    public Object getSource() { return source; }
    public Object getData() { return data; }
    public long getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return "OpenClawEvent{type='" + type + "', timestamp=" + timestamp + "}";
    }
}
