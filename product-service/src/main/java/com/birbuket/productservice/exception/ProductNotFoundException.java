package com.birbuket.productservice.exception;

import com.birbuket.common.enums.ErrorCode;
import com.birbuket.common.exception.BaseException;
import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends BaseException {
    public ProductNotFoundException(String message) {
        super(message, HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND);
    }
}
