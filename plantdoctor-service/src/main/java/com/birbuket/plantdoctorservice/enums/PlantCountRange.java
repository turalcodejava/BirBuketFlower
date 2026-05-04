package com.birbuket.plantdoctorservice.enums;

import java.math.BigDecimal;

public enum PlantCountRange {
    RANGE_1_3(new BigDecimal("15.00")),
    RANGE_4_7(new BigDecimal("30.00")),
    RANGE_8_PLUS(new BigDecimal("50.00"));

    private final BigDecimal fee;

    PlantCountRange(BigDecimal fee) {
        this.fee = fee;
    }

    public BigDecimal fee() {
        return fee;
    }
}
