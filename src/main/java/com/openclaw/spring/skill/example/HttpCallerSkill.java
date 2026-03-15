package com.openclaw.spring.skill.example;

import com.openclaw.spring.event.EventTypes;
import com.openclaw.spring.event.OpenClawEvent;
import com.openclaw.spring.event.OpenClawEventListener;
import com.openclaw.spring.skill.OpenClawSkill;
import com.openclaw.spring.skill.SkillAction;
import com.openclaw.spring.skill.SkillParam;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 示例 Skill：HTTP 调用
 *
 * 演示：
 * - 使用 Java 11 HttpClient 发起外部请求
 * - @OpenClawEventListener 监听事件做统计
 * - 超时控制和错误处理
 *
 * 注意：生产环境中应添加 URL 白名单和更严格的安全控制
 */
@OpenClawSkill(name = "http", description = "HTTP 请求工具", version = "1.0.0")
public class HttpCallerSkill {

    private final HttpClient client;
    private final AtomicLong requestCount = new AtomicLong(0);

    public HttpCallerSkill() {
        this.client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(5))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    @SkillAction(name = "get", description = "发送 GET 请求")
    public String get(@SkillParam(value = "url", description = "目标 URL") String url) {
        try {
            validateUrl(url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            requestCount.incrementAndGet();

            return formatResponse("GET", url, response);
        } catch (Exception e) {
            return formatError("GET", url, e);
        }
    }

    @SkillAction(name = "post", description = "发送 POST 请求")
    public String post(
            @SkillParam(value = "url", description = "目标 URL") String url,
            @SkillParam(value = "body", description = "请求体", required = false) String body) {
        try {
            validateUrl(url);

            HttpRequest.BodyPublisher bodyPublisher = (body != null && !body.isEmpty())
                    ? HttpRequest.BodyPublishers.ofString(body)
                    : HttpRequest.BodyPublishers.noBody();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(bodyPublisher)
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString());

            requestCount.incrementAndGet();

            return formatResponse("POST", url, response);
        } catch (Exception e) {
            return formatError("POST", url, e);
        }
    }

    @SkillAction(name = "head", description = "检查 URL 是否可达")
    public String head(@SkillParam(value = "url", description = "目标 URL") String url) {
        try {
            validateUrl(url);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(5))
                    .method("HEAD", HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(
                    request, HttpResponse.BodyHandlers.discarding());

            requestCount.incrementAndGet();

            return String.format("🔗 %s → HTTP %d (reachable)", url, response.statusCode());
        } catch (Exception e) {
            return formatError("HEAD", url, e);
        }
    }

    @SkillAction(name = "stats", description = "请求统计")
    public String stats() {
        return String.format("📊 HTTP 请求统计：共 %d 次调用", requestCount.get());
    }

    /**
     * 监听 Skill 执行事件，记录日志
     */
    @OpenClawEventListener(EventTypes.SKILL_EXECUTE_COMPLETE)
    public void onSkillExecuted(OpenClawEvent event) {
        // 可以做执行统计、监控等
    }

    private void validateUrl(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL 不能为空");
        }
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("URL 必须以 http:// 或 https:// 开头");
        }
    }

    private String formatResponse(String method, String url, HttpResponse<String> response) {
        int status = response.statusCode();
        String statusEmoji = status < 300 ? "✅" : status < 400 ? "↪️" : "❌";

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%s %s %s → HTTP %d\n", statusEmoji, method, url, status));
        sb.append(String.format("📏 Content-Length: %d bytes\n", response.body().length()));

        // 只显示前 500 字符
        String body = response.body();
        if (body.length() > 500) {
            sb.append("📄 Body (前 500 字符):\n").append(body, 0, 500).append("...");
        } else {
            sb.append("📄 Body:\n").append(body);
        }

        return sb.toString();
    }

    private String formatError(String method, String url, Exception e) {
        return String.format("❌ %s %s 失败：%s", method, url, e.getMessage());
    }
}
