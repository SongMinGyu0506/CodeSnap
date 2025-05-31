package com.bi_side.CodeSnap.config.util;

import lombok.Getter;

@Getter
public enum ErrorCode {
    FAIL(999, "실패"),
    NOT_SUPPORTED_METHOD(998,"지원하지 않는 HTTP METHOD"),
    MALFORMED_ERROR(997, "부정확한 파라미터 입력"),
    DATA_ACCESS_ERROR(998,"DB 접근 에러")
    ;

    private int code;
    private String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
