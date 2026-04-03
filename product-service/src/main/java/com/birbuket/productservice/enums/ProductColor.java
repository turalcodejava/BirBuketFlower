package com.birbuket.productservice.enums;

public enum ProductColor {

    RED("#FF0000"),
    BLUE("#0000FF"),
    GREEN("#00FF00"),
    YELLOW("#FFFF00"),
    BLACK("#000000"),
    WHITE("#FFFFFF"),
    PINK("#FFC0CB"),
    ORANGE("#FFA500"),
    PURPLE("#800080"),
    BROWN("#A52A2A"),
    CYAN("#00FFFF"),
    MAGENTA("#FF00FF"),
    LIME("#00FF00"),
    NAVY("#000080"),
    TEAL("#008080"),
    OLIVE("#808000"),
    MAROON("#800000"),
    SILVER("#C0C0C0"),
    GRAY("#808080"),
    GOLD("#FFD700");

    private final String hexCode;

    ProductColor(String hexCode) {
        this.hexCode = hexCode;
    }

    public String getHexCode() {
        return hexCode;
    }
}