package com.graydang.app.crawler;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SummaryCrawler {

    public String extractSummary(String summaryLink) {
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
