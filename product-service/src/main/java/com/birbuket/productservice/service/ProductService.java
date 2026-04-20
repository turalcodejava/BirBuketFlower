package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.product.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException;

    ProductByIdResponse getProductBySlug(String slug);

    Page<ProductByIdResponse> getViewAllProducts(int size, int page);

    void deleteProduct(Long id);

    UpdateProductResponse updateProductResponse(Long id, UpdateProductRequest request);

    Page<ProductByCategoryResponse> getProductByCategoryId(Long categoryId, int size, int page);
}
