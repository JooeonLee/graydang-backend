package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "response")
@Data
public class BillDeliverateInfoResponseDto {

    private HeaderDto header;
    private BodyDto body;

    @Data
    public static class HeaderDto {
        private String requestMsgID;
        private String responseTime;
        private String responseMsgID;
        private String successYN;
        private String resultCode;
        private String resultMsg;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BodyDto {
        @JacksonXmlElementWrapper(localName = "PlenarySessionExamination")
        @JacksonXmlProperty(localName = "item")
        private List<PlenarySessionExaminationItem> plenarySessionExamination;

        @JsonIgnore
        @JacksonXmlElementWrapper(localName = "PlenarySessionModify")
        @JacksonXmlProperty(localName = "item")
        private List<PlenarySessionModifyItem> plenarySessionModify;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlenarySessionExaminationItem {
        private String confName;
        private String procDt;
        private String procResultCd;
        private String procResultContent;
        private String prsntDt;
        private String fileUrl;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PlenarySessionModifyItem {
        private String PProcDt;
        private String PProcResultCd;
        private String proposeDt;
        private String proposer;
        private String proposeId;
        private String pdfUrl;
        private String hwpUrl;
    }
}
