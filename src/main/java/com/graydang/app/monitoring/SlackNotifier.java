package com.graydang.app.monitoring;

public interface SlackNotifier {
  void send(SlackPayload payload);
}
