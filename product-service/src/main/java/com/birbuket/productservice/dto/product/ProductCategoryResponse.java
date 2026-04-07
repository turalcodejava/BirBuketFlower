package com.birbuket.productservice.dto.product;

import lombok.Data;

@Data
public class ProductCategoryResponse {
    private Long id;
    private String title;
    private String subtitle;
    private String imageUrl;
}
