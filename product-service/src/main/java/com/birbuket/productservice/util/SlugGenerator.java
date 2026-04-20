package com.birbuket.productservice.util;


public class SlugGenerator {

    public static String generatorSlug(String productName){
        productName = productName.replaceAll(" ","-").toLowerCase() + System.currentTimeMillis();
        return productName;
    }
}
