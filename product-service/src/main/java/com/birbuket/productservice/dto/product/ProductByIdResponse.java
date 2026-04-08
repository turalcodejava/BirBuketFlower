package com.birbuket.productservice.dto.product;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
public class ProductByIdResponse {
    private Long id;
    private String productName;
    private String description;
    private String composition;
    private BigDecimal discountPercentage;
    private boolean active;
    private boolean isSingle;
    private BigDecimal rating;
    private Integer reviewCount;
    private String slug;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String sku;
    private ProductCategoryResponse productCategory;

    private List<ProductImageResponse> images;
    private List<ProductVariantResponse> productVariants;
}
