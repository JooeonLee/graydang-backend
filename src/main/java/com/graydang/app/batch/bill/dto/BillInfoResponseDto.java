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
public class BillInfoResponseDto {

    private HeaderDto header;
    private BodyDto body;

    @Data
    public static class HeaderDto {
        private String resultCode;
        private String resultMsg;
    }

    @Data
    public static class BodyDto {
        @JacksonXmlElementWrapper(localName = "items")
        @JacksonXmlProperty(localName = "item")
        private List<ItemDto> items;

        @JacksonXmlProperty(localName = "numOfRows")
        private int numOfRows;

        @JacksonXmlProperty(localName = "pageNo")
        private int pageNo;

        @JacksonXmlProperty(localName = "totalCount")
        private int totalCount;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ItemDto {
        private String billId;
        private String billName;
        private String billNo;
        private String generalResult;
        private String passGubn;
        private String procDt;
        private String procStageCd;
        private String proposeDt;
        private String proposerKind;
        private String summary;
    }
}

