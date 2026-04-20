package com.birbuket.productservice.util;

import java.time.LocalDateTime;

public class SkuGenerator {

    public static String generatorSku(){
        String sku = "#" + LocalDateTime.now();
        return sku;
    }
}
