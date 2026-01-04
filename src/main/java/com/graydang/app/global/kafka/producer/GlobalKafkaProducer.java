package com.graydang.app.global.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GlobalKafkaProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * 메시지 전송 메서드
     * @param topic   토픽 이름 (예: "bill-notification-dev")
     * @param key     구분자 Key (예: "BILL_12345")
     * @param message 보낼 내용 (아무 객체나 가능)
     */
    public void send(String topic, String key, Object message) {
        log.info("🚀 [Kafka] Sending to Topic: {}, Key: {}", topic, key);

        try {
            kafkaTemplate.send(topic, key, message);
            log.info("✅ [Kafka] Sent successfully.");
        } catch (Exception e) {
            log.error("❌ [Kafka] Failed to send message.", e);
        }
    }
}
