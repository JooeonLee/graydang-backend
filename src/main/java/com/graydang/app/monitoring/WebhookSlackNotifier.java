package com.graydang.app.monitoring;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import software.amazon.awssdk.services.s3.endpoints.internal.Value.Int;

public class WebhookSlackNotifier implements SlackNotifier {
  private final WebClient client;
  private static final String WEBHOOK_URL = "https://hooks.slack.com/services/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";
  private static int TIMEOUT_MS = 1000;

  public WebhookSlackNotifier(WebClient client) {
    this.client = client;

  }

  @Override
  public void send(SlackPayload payload) {
    var body = new java.util.HashMap<String, Object>();
    body.put("text", payload.text() == null ? "(no text)" : payload.text());

    client.post()
        .uri(WEBHOOK_URL)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .bodyToMono(String.class)
        .timeout(java.time.Duration.ofMillis(TIMEOUT_MS))
        .onErrorResume(e -> { // 실패 로깅만
          org.slf4j.LoggerFactory.getLogger(getClass())
              .warn("Slack webhook send failed: {}", e.toString());
          return Mono.empty();
        })
        .block(); // 운영상 알림은 보통 즉시 전송. 비동기로 하고 싶으면 subscribe().
  }
}
