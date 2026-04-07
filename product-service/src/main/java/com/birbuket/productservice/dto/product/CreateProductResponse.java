package com.birbuket.productservice.dto.product;

import com.birbuket.productservice.enums.ProductSize;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateProductResponse {
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
    private List<ProductSize> productSizes;
    private List<ProductVariantResponse> productVariants;
}
