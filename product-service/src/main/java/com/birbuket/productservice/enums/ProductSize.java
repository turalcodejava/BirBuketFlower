package com.birbuket.productservice.enums;

public enum ProductSize {
    CM30(30),
    CM40(40),
    CM50(50),
    CM60(60),
    CM70(70);

    private final int lengthCm;

    ProductSize(int lengthCm) {
        this.lengthCm = lengthCm;
    }

    public int getLengthCm() {
        return lengthCm;
    }

    @Override
    public String toString() {
        return lengthCm + " cm";
    }
}