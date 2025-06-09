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
    NONE_USER(false, HttpStatus.NOT_FOUND, 2601, "존재하지 않는 사용자입니다."),
    INVALID_USER_KEYWORD(false, HttpStatus.NOT_FOUND, 2602, "유효하지 않은 사용자 키워드입니다."),

    /**
     * 2700 : File exception
     */
    INVALID_FILE_TYPE(false, HttpStatus.BAD_REQUEST, 2701, "지원하지 않는 파일 확장자입니다."),
    MALFORMED_UPLOAD_FILE(false, HttpStatus.BAD_REQUEST, 2702, "손상된 upload 파일입니다."),

    /**
     * 2800 : S3 exception
     */
    S3_UPLOAD_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, 2801, "S3 파일 업로드에 실패했습니다."),
    S3_URL_GENERATION_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, 2802, "S3 파일 URL 생성에 실패했습니다."),
    S3_FILE_DELETE_FAILED(false, HttpStatus.INTERNAL_SERVER_ERROR, 2803, "S3 파일 삭제에 실패했습니다."),
    INVALID_S3_URI(false, HttpStatus.BAD_REQUEST, 2804, "S3 파일 정보를 확인할 수 없습니다."),

    /**
     * 2900 : JWT & 인증 관련 exception
     */
    INVALID_TOKEN(false, HttpStatus.UNAUTHORIZED, 2901, "유효하지 않은 JWT 토큰입니다."),
    EXPIRED_TOKEN(false, HttpStatus.UNAUTHORIZED, 2902, "만료된 JWT 토큰입니다."),
    BLACKLISTED_REFRESH_TOKEN(false, HttpStatus.UNAUTHORIZED, 2903, "사용이 중지된 리프레시 토큰입니다."),
    NOT_REFRESH_TOKEN(false, HttpStatus.BAD_REQUEST, 2904, "리프레시 토큰이 아닙니다.");

    private final boolean isSuccess;
    @JsonIgnore
    private final HttpStatus httpStatus;
    private final int responseCode;
    private final String responseMessage;
}
