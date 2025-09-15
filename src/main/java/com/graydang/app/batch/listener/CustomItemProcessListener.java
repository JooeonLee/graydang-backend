package com.graydang.app.batch.listener;

import com.graydang.app.monitoring.SlackBotNotifier;
import com.graydang.app.monitoring.SlackNotifier;
import com.graydang.app.monitoring.SlackPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ItemProcessListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomItemProcessListener implements ItemProcessListener<Object, Object> {
    private final SlackBotNotifier slackBotNotifier;

    @Override
    public void onProcessError(Object item, Exception e) {
        log.error("Item 처리 중 오류 발생! Item: {}, Error: {}", item, e.getMessage());

        // 1. mrkdwn 문법을 사용해 보기 좋은 텍스트 메시지를 만듭니다.
        String formattedMessage = String.format(
                """
                🚨 *배치 아이템 처리 실패!*
                > *Error Message:*
                > ```%s```
                > *Failed Item:*
                > ```%s```
                """,
                e.getMessage(),
                item.toString()
        );

        // 2. SlackPayload의 'text' 필드에만 메시지를 담아 생성합니다.
        // (channel과 blocks는 "#monitoring", true로 전달합니다.)
        SlackPayload payload = new SlackPayload(formattedMessage, "#monitoring", null );

        // 3. 기존 SlackBotNotifier의 send 메소드를 호출합니다.
        slackBotNotifier.send(payload);
    }

}
