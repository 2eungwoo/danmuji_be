package com.back2basics.infra.exception.global;

import com.back2basics.global.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "GEN001", "접근 권한이 없습니다."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

}
