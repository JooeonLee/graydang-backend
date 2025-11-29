package com.graydang.app.monitoring;

public interface SlackNotifier {
  void send(SlackPayload payload);

  void send(String channel, SlackPayload payload);
}
