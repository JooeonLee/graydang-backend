package com.graydang.app.monitoring;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class SlackBotNotifier implements SlackNotifier {

  private final WebClient client;
  private static final String CHANNEL_ID = "monitoring"; // ✅ 채널 ID로 교체
  private static final Duration TIMEOUT = Duration.ofSeconds(2);

  public SlackBotNotifier(WebClient client) {
    this.client = client;
  }

  @Override
  public void send(SlackPayload payload) {
    System.out.println("slack bot notifier");

    Map<String, Object> body = new HashMap<>();
    body.put("channel", CHANNEL_ID);
    if (payload.text() != null && !payload.text().isBlank()) {
      body.put("text", payload.text());
    }

    var res = client.post()
        .uri("/chat.postMessage")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(body)
        .retrieve()
        .onStatus(s -> s.value() == 429, resp -> resp.createException()) // 간단 처리
        .bodyToMono(String.class)
        .block(TIMEOUT); // ✅ 실제 전송 트리거

    System.out.println("[Slack Response] " + res);
  }
}



