package com.graydang.app.batch.bill.dto;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JacksonXmlRootElement(localName = "RESULT")
public class ResultOnlyResponseDto {

    @JacksonXmlProperty(localName = "CODE")
    private String code;

    @JacksonXmlProperty(localName = "MESSAGE")
    private String message;
}