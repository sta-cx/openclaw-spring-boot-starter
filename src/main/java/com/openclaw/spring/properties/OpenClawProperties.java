package com.openclaw.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "openclaw")
public class OpenClawProperties {

    private Gateway gateway = new Gateway();
    private WebSocket websocket = new WebSocket();
    private Security security = new Security();
    private Plugin plugin = new Plugin();

    public Gateway getGateway() {
        return gateway;
    }

    public void setGateway(Gateway gateway) {
        this.gateway = gateway;
    }

    public WebSocket getWebsocket() {
        return websocket;
    }

    public void setWebsocket(WebSocket websocket) {
        this.websocket = websocket;
    }

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public void setPlugin(Plugin plugin) {
        this.plugin = plugin;
    }

    public static class Gateway {
        private String url = "http://localhost:18789";
        private String token;
        private int timeout = 30;

        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }
        public int getTimeout() { return timeout; }
        public void setTimeout(int timeout) { this.timeout = timeout; }
    }

    public static class WebSocket {
        private boolean enabled = true;
        private String path = "/ws/events";
        private int reconnectInterval = 5;     // seconds
        private int maxReconnectAttempts = 10;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        public int getReconnectInterval() { return reconnectInterval; }
        public void setReconnectInterval(int reconnectInterval) { this.reconnectInterval = reconnectInterval; }
        public int getMaxReconnectAttempts() { return maxReconnectAttempts; }
        public void setMaxReconnectAttempts(int maxReconnectAttempts) { this.maxReconnectAttempts = maxReconnectAttempts; }
    }

    public static class Security {
        private boolean enabled = false;
        private List<String> apiKeys = List.of();
        private String protectedPath = "/api/openclaw";

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public List<String> getApiKeys() { return apiKeys; }
        public void setApiKeys(List<String> apiKeys) { this.apiKeys = apiKeys; }
        public String getProtectedPath() { return protectedPath; }
        public void setProtectedPath(String protectedPath) { this.protectedPath = protectedPath; }
    }

    public static class Plugin {
        private boolean enabled = false;
        private String directory = "./plugins";
        private boolean hotReload = true;

        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDirectory() { return directory; }
        public void setDirectory(String directory) { this.directory = directory; }
        public boolean isHotReload() { return hotReload; }
        public void setHotReload(boolean hotReload) { this.hotReload = hotReload; }
    }
}
