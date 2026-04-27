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
    /** Şifrə sıfırlama token-i yanlış / istifadə olunub / müddəti bitib */
    PASSWORD_RESET_INVALID("AUTH007"),
    FORBIDDEN("AUTHR006"),
    CATEGORY_ALREADY_EXISTS("CATEGORY001"),
    CATEGORY_NOT_FOUND("CATEGORY002"),
    PRODUCT_ALREADY_EXISTS("PRODUCT001"),
    PRODUCT_NOT_FOUND("PRODUCT002"),
    VARIANTS_NOT_FOUND("VARIANT001"),
    INVALID_REQUEST("COMMON001"),
    DATA_CONFLICT("COMMON002"),
    INTERNAL_ERROR("COMMON003");

    private final String code;

}
