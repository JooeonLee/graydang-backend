package com.graydang.app.global.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();

        // UTF-8 인코딩을 적용한 StringHttpMessageConverter 추가
        List<StringHttpMessageConverter> converters = restTemplate.getMessageConverters().stream()
                .filter(converter -> converter instanceof StringHttpMessageConverter)
                .map(converter -> (StringHttpMessageConverter) converter)
                .toList();

        for (StringHttpMessageConverter converter : converters) {
            converter.setDefaultCharset(StandardCharsets.UTF_8);
        }

        return restTemplate;
    }
}
