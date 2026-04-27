package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException;

    ProductBySlugResponse getProductBySlug(String slug);

    Page<ProductBySlugResponse> getViewAllProducts(int size, int page);

    void deleteProduct(Long id);

    UpdateProductResponse updateProductResponse(Long id, UpdateProductRequest request);

    Page<ProductByCategoryResponse> getProductByCategory(String categoryKey, int size, int page);

    List<ProductBySlugResponse> filterProducts(Long categoryId, String category, BigDecimal minPrice, BigDecimal maxPrice);
}
