package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
@Data
public class BillPromulgationInfoResponseDto {

    @JacksonXmlProperty(localName = "header")
    private HeaderDto header;

    @JacksonXmlProperty(localName = "body")
    private BodyDto body;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class HeaderDto {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BodyDto {

        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<PromulgationItem> items;

        private int totalCount;
        private int pageNo;
        private int numOfRows;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PromulgationItem {
        private String anounceDt;      // 공포일자
        private String anounceNo;      // 공포번호
        private String lawTitle;       // 공포법률명
        private String lawBonUrl;      // 법령 원문 URL
    }
}
