package com.birbuket.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import com.birbuket.common.enums.ErrorCode;

@RequiredArgsConstructor
@Getter
public class BaseException extends RuntimeException {
    private final HttpStatus status;
    private final ErrorCode errorcode;

    public BaseException(String message,  HttpStatus status,  ErrorCode errorcode) {
        super(message);
        this.status = status;
        this.errorcode = errorcode;
    }
}
