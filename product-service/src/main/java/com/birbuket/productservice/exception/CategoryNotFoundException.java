package com.birbuket.productservice.exception;

import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class CategoryNotFoundException extends BaseException {
    public CategoryNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.CATEGORY_NOT_FOUND);
    }
}
