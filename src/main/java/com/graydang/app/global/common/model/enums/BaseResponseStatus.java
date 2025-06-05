package com.graydang.app.global.common.model.enums;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus {

    /**
     * 1000 : 요청 성공
     */
    SUCCESS(true, HttpStatus.OK,1000, "요청에 성공하였습니다."),

    /**
     * 2000 : RequestParam exception
     */
    METHOD_ARGUMENT_TYPE_MISMATCH(false, HttpStatus.BAD_REQUEST, 2001, "요청 파라미터 타입이 올바르지 않습니다."),
    HTTP_METHOD_TYPE_MISMATCH(false, HttpStatus.METHOD_NOT_ALLOWED, 2002, "지원되지 않는 Http Method입니다."),


    /**
     * 2500 : Bill exception
     */
    NONE_BILL(false, HttpStatus.NOT_FOUND, 2501, "존재하지 않는 의안입니다."),

    /**
     * 2600 : User exception
     */
    NONE_USER(false, HttpStatus.NOT_FOUND, 2601, "존재하지 않는 사용자입니다.");


    private final boolean isSuccess;
    @JsonIgnore
    private final HttpStatus httpStatus;
    private final int responseCode;
    private final String responseMessage;
}
