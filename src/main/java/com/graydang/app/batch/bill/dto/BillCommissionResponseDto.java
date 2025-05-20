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
public class BillCommissionResponseDto {

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
        @JacksonXmlElementWrapper(localName = "JurisdictionExamination")
        @JacksonXmlProperty(localName = "item")
        private List<JurisdictionExaminationItem> jurisdictionExamination;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class JurisdictionExaminationItem {
        @JacksonXmlProperty(localName = "committeeName")
        private String committeeName;

        @JacksonXmlProperty(localName = "docName1")
        private String docName1;

        @JacksonXmlProperty(localName = "docName2")
        private String docName2;

        @JacksonXmlProperty(localName = "hwpUrl1")
        private String hwpUrl1;

        @JacksonXmlProperty(localName = "hwpUrl2")
        private String hwpUrl2;

        @JacksonXmlProperty(localName = "pdfUrl1")
        private String pdfUrl1;

        @JacksonXmlProperty(localName = "pdfUrl2")
        private String pdfUrl2;

        @JacksonXmlProperty(localName = "presentDt")
        private String presentDt;

        @JacksonXmlProperty(localName = "procDt")
        private String procDt;

        @JacksonXmlProperty(localName = "procResultCd")
        private String procResultCd;

        @JacksonXmlProperty(localName = "submitDt")
        private String submitDt;
    }
}
