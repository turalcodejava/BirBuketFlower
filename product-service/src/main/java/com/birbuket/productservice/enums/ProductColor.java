package com.birbuket.productservice.enums;

public enum ProductColor {

    RED("#FF0000", "Red"),
    BLUE("#0000FF", "Blue"),
    GREEN("#00FF00", "Green"),
    YELLOW("#FFFF00", "Yellow"),
    BLACK("#000000", "Black"),
    WHITE("#FFFFFF", "White"),
    PINK("#FFC0CB", "Pink"),
    ORANGE("#FFA500", "Orange"),
    PURPLE("#800080", "Purple"),
    BROWN("#A52A2A", "Brown"),
    CYAN("#00FFFF", "Cyan"),
    MAGENTA("#FF00FF", "Magenta"),
    LIME("#00FF00", "Lime"),
    NAVY("#000080", "Navy"),
    TEAL("#008080", "Teal"),
    OLIVE("#808000", "Olive"),
    MAROON("#800000", "Maroon"),
    SILVER("#C0C0C0", "Silver"),
    GRAY("#808080", "Gray"),
    GOLD("#FFD700", "Gold");

    private final String hexCode;
    private final String colorName;

    ProductColor(String hexCode, String colorName) {
        this.hexCode = hexCode;
        this.colorName = colorName;
    }

    public String getHexCode() {
        return hexCode;
    }

    public String getColorName() {
        return colorName;
    }
}