package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.product.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductService {

    CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException;

    ProductByIdResponse getProductById(Long id);

    List<ProductByIdResponse> getViewAllProducts();

    void deleteProduct(Long id);

    UpdateProductResponse updateProductResponse(Long id, UpdateProductRequest request);
}
