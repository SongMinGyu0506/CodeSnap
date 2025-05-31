package com.bi_side.CodeSnap.config.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import java.sql.Timestamp;

@Getter
@Setter
public class ApiResponse<T> {
    //httpStatus 코드
    private int httpStatus = HttpStatus.OK.value();
    // ERROR 발생 시 출력할 메시지
    private String errorMsg;
    // API 호출 시점
    private Timestamp responseTime = new Timestamp(System.currentTimeMillis());
    //출력할 데이터
    private T data;

    //성공 시 출력
    public static <T> ApiResponse<T> OK(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setHttpStatus(HttpStatus.OK.value());
        response.setData(data);
        
        return response;
    }

    //실패 시 출력
    public static <T> ApiResponse<T> fail(T data, ErrorCode errorCode) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setHttpStatus(errorCode.getCode());
        response.setErrorMsg(errorCode.getMessage());

        return response;
    }
}
