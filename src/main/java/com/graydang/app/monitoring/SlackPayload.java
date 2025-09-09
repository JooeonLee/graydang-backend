package com.graydang.app.monitoring;

public record SlackPayload(
    String text,                 // 폴백 텍스트 (필수 권장)
    String channel,              // Bot 방식일 때만 의미 있음
    Object blocks                // Block Kit JSON (Map/List 혹은 Jackson tree)
) {
}
