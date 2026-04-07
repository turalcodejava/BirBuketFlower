package com.birbuket.common.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND("AUTH001"),
    USER_IS_NOT_ACTIVE("AUTH002"),
    USER_ALREADY_EXISTS("AUTH003"),
    PASSWORD_MISMATCH("AUTH004"),
    UNAUTHORIZED("AUTH005"),
    KEYCLOAK_PROVISIONING_FAILED("AUTH006"),
    FORBIDDEN("AUTHR006"),
    CATEGORY_ALREADY_EXISTS("CATEGORY001"),
    CATEGORY_NOT_FOUND("CATEGORY002"),
    PRODUCT_ALREADY_EXISTS("PRODUCT001");

    private final String code;

}
