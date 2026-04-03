package com.birbuket.productservice.mapper;

import com.birbuket.productservice.dto.product.CreateProductRequest;
import com.birbuket.productservice.dto.product.CreateProductResponse;
import com.birbuket.productservice.models.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    @Mapping(target = "images", ignore = true)
    @Mapping(target = "productVariants", ignore = true)
    @Mapping(target = "productReviews", ignore = true)
    @Mapping(target = "productCategory", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toProduct(CreateProductRequest request);

    CreateProductResponse toProductResponse(Product savedProduct);
}
