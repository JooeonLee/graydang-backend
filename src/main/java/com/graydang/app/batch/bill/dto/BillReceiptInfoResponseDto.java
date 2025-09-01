package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
public class BillReceiptInfoResponseDto {
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
        private ItemDto item;
    }

    @Data
    public static class ItemDto {
        @JacksonXmlProperty(localName = "receipt")
        private Receipt receipt;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Receipt {
        private String billName;
        private String billNo;
        private String proposeDt;
        private String proposer;
        private String summaryLink;
        private String withdrawDate;
    }
}
