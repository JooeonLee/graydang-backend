package com.graydang.app.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class SummaryCrawler {

    private static final String SUMMARY_POPUP_URL = "https://likms.assembly.go.kr/bill/bi/popup/billSummary.do?billId=";

    public String extractSummary(String billId) {
        // 전체 URL을 조합합니다.
        String fullUrl = SUMMARY_POPUP_URL + billId;

        try {
            // 2. Jsoup을 사용해 해당 URL에 접속하고 HTML 문서를 가져옵니다.
            Document doc = Jsoup.connect(fullUrl).get();

            // 3. CSS 선택자(Selector)를 사용해 원하는 내용이 담긴 <pre> 태그를 찾습니다.
            // <div class="pop_cont"> 태그 안에 있는 <pre> 태그를 선택합니다.
            Element preElement = doc.selectFirst("div.pop_cont pre");

            // 4. 태그를 찾았는지 확인하고 텍스트를 추출합니다.
            if (preElement != null) {
                String rawText = preElement.text();
                // 5. 불필요한 앞부분 제목을 제거하고 앞뒤 공백을 정리하여 반환합니다.
                return rawText.replace("제안이유 및 주요내용", "").trim();
            } else {
                log.warn("Could not find summary element for billId: {}", billId);
                return null;
            }

        } catch (IOException e) {
            log.error("Failed to crawl summary for billId: {}, URL: {}", billId, fullUrl, e);
            // 예외 발생 시 null을 반환하여 Batch의 skip 로직이 동작하도록 합니다.
            throw new RuntimeException("Crawling failed for billId: " + billId, e);
        }
    }

    public String extractSummary2(String summaryLink) {
        try  {
            Document doc = Jsoup.connect(summaryLink)
                    .userAgent("Mozilla/5.0")
                    .timeout(10_000)
                    .get();

            Element content = doc.selectFirst("div.textType02.mt30, #summaryContentDiv");
            if (content == null) {
                return null;
            }

            return content.html()
                    .replace("<br>", "\n")
                    .replace("<br/>", "\n")
                    .replace("<br />", "\n")
                    .replaceAll("<[^>]+>", "")
                    .trim();
        } catch (Exception e) {
            log.warn("Summary crawler failed to extract summary link : {}", summaryLink);
            return null;
        }
    }
}
