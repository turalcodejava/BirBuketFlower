package com.birbuket.productservice.exception;

import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryAlreadyExistsException extends BaseException {
    public CategoryAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.CATEGORY_ALREADY_EXISTS);
    }
}
