package com.graydang.app.global.logging.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.graydang.app.global.logging.config.LoggingProperties;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class HttpRequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(HttpRequestLoggingFilter.class);
    private static final int MAX_LOG_LENGTH = 1024;
    private static final ObjectWriter PRETTY_PRINTER = new ObjectMapper().writerWithDefaultPrettyPrinter();
    private final LoggingProperties loggingProperties;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (!loggingProperties.getHttpRequest()) {
            chain.doFilter(request, response);
            return;
        }

        if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper httpRequest = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper httpResponse = new ContentCachingResponseWrapper((HttpServletResponse) response);

        String method = httpRequest.getMethod();
        String uri = httpRequest.getRequestURI();

        log.info("[HTTP] START {} {}", method, uri);

        long start = System.currentTimeMillis();
        try {
            chain.doFilter(httpRequest, httpResponse); // 다음 필터 또는 컨트롤러로 요청 전달
        } finally {
            long duration = System.currentTimeMillis() - start;
            int status = httpResponse.getStatus();

            String requestBody = new String(httpRequest.getContentAsByteArray(), httpRequest.getCharacterEncoding());
            if (requestBody.length() > MAX_LOG_LENGTH) {
                requestBody = requestBody.substring(0, MAX_LOG_LENGTH) + "...";
            }
            try {
                Object json = new ObjectMapper().readValue(requestBody, Object.class);
                log.info("[HTTP] Request Body:\n{}", PRETTY_PRINTER.writeValueAsString(json));
            } catch (Exception e) {
                log.info("[HTTP] Request Body (raw): {}", requestBody); // JSON이 아닐 경우
            }

            byte[] responseArray = httpResponse.getContentAsByteArray();
            String responseBody = new String(responseArray, StandardCharsets.UTF_8);
            if (responseBody.length() > MAX_LOG_LENGTH) {
                responseBody = responseBody.substring(0, MAX_LOG_LENGTH) + "...";
            }
            try {
                Object json = new ObjectMapper().readValue(responseBody, Object.class);
                log.info("[HTTP] Response Body:\n{}", PRETTY_PRINTER.writeValueAsString(json));
            } catch (Exception e) {
                log.info("[HTTP] Response Body (raw): {}", responseBody); // JSON이 아닐 경우
            }

            log.info("[HTTP] END   {} {} ({} ms)", status, uri, duration);
            httpResponse.copyBodyToResponse(); // 응답 바디 다시 복사
        }
    }

    // TODO application.yml 설정을 통해 운영 환경에서는 바디 로깅 off 하기
}
