package com.birbuket.productservice.exception;

import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductNameAlreadyExistsException extends BaseException {
    public ProductNameAlreadyExistsException(String message) {
        super(message, HttpStatus.CONFLICT, ErrorCode.PRODUCT_ALREADY_EXISTS);
    }
}
