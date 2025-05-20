package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "response")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillTransferredInfoResponseDto {

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
        private List<TransferredItem> items;

        private int totalCount;
        private int pageNo;
        private int numOfRows;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TransferredItem {
        private String transDt; // 정부 이송일
    }
}
