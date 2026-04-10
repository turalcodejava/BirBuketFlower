package com.birbuket.productservice.exception;

import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class VariantsNotFoundException extends BaseException {
    public VariantsNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.VARIANTS_NOT_FOUND );
    }
}
