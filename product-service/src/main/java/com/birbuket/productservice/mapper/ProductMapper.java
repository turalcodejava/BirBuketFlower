package com.birbuket.productservice.mapper;

import com.birbuket.productservice.dto.category.ProductCategoryResponse;
import com.birbuket.productservice.dto.product.*;
import com.birbuket.productservice.dto.product.images.ProductImageResponse;
import com.birbuket.productservice.dto.product.variants.CreateVariantsResponse;
import com.birbuket.productservice.dto.product.variants.ProductVariantResponse;
import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.models.ProductImage;
import com.birbuket.productservice.models.ProductVariant;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

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

    @Mapping(target = "productCategory", source = "productCategory")
    @Mapping(target = "images", source = "images")
    @Mapping(target = "productVariants", source = "productVariants")
    CreateProductResponse toProductResponse(Product product);

    ProductImageResponse toProductImageResponse(ProductImage image);

    ProductVariantResponse toProductVariantResponse(ProductVariant variant);

    ProductCategoryResponse toProductCategoryResponse(ProductCategory category);

    ProductByIdResponse toProductByIdResponse(Product product);

    List<ProductByIdResponse> toALlProduct(List<Product> products);

    UpdateProductResponse updateProductResponse(Product product);

    @Mapping(target = "productId", source = "product.id")
    CreateVariantsResponse toCreateVariantsResponse(ProductVariant savedVariant);
}
