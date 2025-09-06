package com.graydang.app.config;

import com.graydang.app.monitoring.SlackNotifier;
import com.graydang.app.monitoring.WebhookSlackNotifier;
import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
class SlackConfig {

  @Bean
  WebClient slackWebClient() {
    var strategies = ExchangeStrategies.builder()
        .codecs(c -> c.defaultCodecs().maxInMemorySize(256 * 1024))
        .build();

    HttpClient http = HttpClient.create()
        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
        .responseTimeout(Duration.ofMinutes(1));

    return WebClient.builder()
        .clientConnector(new ReactorClientHttpConnector(http))
        .exchangeStrategies(strategies)
        .defaultHeader("Content-Type", "application/json")
        .build();
  }

  @Bean
  SlackNotifier slackNotifier(WebClient slackWebClient) {
    return new WebhookSlackNotifier(slackWebClient);
  }
}
