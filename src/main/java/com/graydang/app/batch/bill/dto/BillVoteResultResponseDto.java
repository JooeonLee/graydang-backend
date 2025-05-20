package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "ncocpgfiaoituanbr")
@JsonIgnoreProperties(ignoreUnknown = true)
public class BillVoteResultResponseDto {

    @JacksonXmlProperty(localName = "head")
    private Head head;

    @JacksonXmlProperty(localName = "row")
    private Item item;

    @Getter
    @Setter
    public static class Head {
        @JacksonXmlProperty(localName = "list_total_count")
        private int listTotalCount;

        @JacksonXmlProperty(localName = "RESULT")
        private Result result;
    }

    @Getter
    @Setter
    public static class Result {
        @JacksonXmlProperty(localName = "CODE")
        private String code;

        @JacksonXmlProperty(localName = "MESSAGE")
        private String message;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Item {
        @JacksonXmlProperty(localName = "BILL_ID")
        private String billId;

        @JacksonXmlProperty(localName = "PROC_DT")
        private String procDate;

        @JacksonXmlProperty(localName = "YES_TCNT")
        private int yesCount;

        @JacksonXmlProperty(localName = "NO_TCNT")
        private int noCount;

        @JacksonXmlProperty(localName = "BLANK_TCNT")
        private int abstentionCount;

        @JacksonXmlProperty(localName = "VOTE_TCNT")
        private int voteTotalCount;

        @JacksonXmlProperty(localName = "MEMBER_TCNT")
        private int memberTotalCount;

        @JacksonXmlProperty(localName = "PROC_RESULT_CD")
        private String procResultCd;
    }
}