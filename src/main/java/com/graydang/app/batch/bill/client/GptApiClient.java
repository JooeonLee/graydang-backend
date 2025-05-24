package com.graydang.app.batch.bill.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.graydang.app.batch.bill.dto.BillGptSummaryDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.json.XMLTokener.entity;

@Component
@RequiredArgsConstructor
@Slf4j
public class GptApiClient {

    @Value("${openai.api.key}")
    private String apiKey;
    @Value("${openai.api.model}")
    private String model;
    @Value("${openai.api.url}")
    private String apiUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public BillGptSummaryDto generateTitleAndSummary(String billTitle, String billSummary) {
        String prompt = buildPrompt(billTitle, billSummary);

        Map<String, Object> request = Map.of(
                "model", model,
                "messages", List.of(
                        Map.of("role", "system", "content", "당신은 법안 요약하고 쉽게 설명하는 전문가입니다."),
                        Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(apiUrl, HttpMethod.POST, entity, Map.class);
            String content = extractMessageContent(response);

            // return objectMapper.readValue(content, BillGptSummaryDto.class);
            return parseJsonResponse(content);
        } catch (Exception e) {
            log.error("GPT 요약 요청 실패", e);
            throw new RuntimeException("GPT 요청 실패: " + e.getMessage());
        }
    }

    private String buildPrompt(String title, String content) {
        if (content == null || content.trim().isEmpty()) {
            content = "내용 없음";
        }
        return String.format("""
            다음은 국회에 제출된 법안의 제목과 ‘제안이유 및 주요내용’입니다. 다음 두 가지를 작성해 주세요.

            1. 일반 국민이 이해하기 쉬운 간결한 법안 제목 (한 문장)
            2. 전체 제안이유 및 주요내용을 1줄로 요약한 문장

            반드시 아래 JSON 형식으로만 응답해 주세요. **절대 다른 문장이나 설명을 포함하지 마세요.**
            ```
            {
               "title": "...",
               "summary": "..."
            }
            ```

            [법안 제목]
            %s

            [제안이유 및 주요내용]
            %s
            """, title, content);
    }

    private String extractMessageContent(ResponseEntity<Map> response) {
        // 1. 응답 본문에서 "choices" 리스트 추출
        Map<String, Object> choice = ((List<Map<String, Object>>) response.getBody().get("choices")).get(0);

        // 2. choice 내 "message" 객체 추출
        Map<String, Object> message = (Map<String, Object>) choice.get("message");

        // 3. message 내 "content" 문자열 추출 → 이게 우리가 원하는 GPT의 응답 텍스트
        return (String) message.get("content");
    }

    private BillGptSummaryDto parseJsonResponse(String content) throws Exception {
        // JSON 시작과 끝 위치 찾기
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");

        if (start >= 0 && end > start) {
            String jsonPart = content.substring(start, end + 1);
            return objectMapper.readValue(jsonPart, BillGptSummaryDto.class);
        } else {
            throw new IllegalArgumentException("유효한 JSON 형식을 찾을 수 없습니다:\n" + content);
        }
    }
}
