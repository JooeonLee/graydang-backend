package com.graydang.app.global.kafka.test;

import com.graydang.app.global.kafka.producer.GlobalKafkaProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class KafkaConnectionTestRunner implements CommandLineRunner {

    private final GlobalKafkaProducer producer;

    @Override
    public void run(String... args) throws Exception {
        log.info("===============================");
        log.info("📢 [TEST] 카프카 연결 테스트 시작...");

        producer.send("test-connection-topic", "test-key-01", "Hello Kafka! Can you hear me?");

        log.info("===============================");
    }
}
