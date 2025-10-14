package com.back2basics.infra.exception.global;

import com.back2basics.global.response.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "GEN001", "접근 권한이 없습니다."),
    LOCK_ACQUISITION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "GEN002", "요청을 처리하는 중입니다. 잠시 후 다시 시도해주세요."),
    ;

    private final HttpStatus status;
    private final String code;
    private final String message;

}
