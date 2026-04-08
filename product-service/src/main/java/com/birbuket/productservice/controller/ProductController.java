package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.dto.product.CreateProductRequest;
import com.birbuket.productservice.dto.product.CreateProductResponse;
import com.birbuket.productservice.dto.product.ProductVariantRequest;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(value = "/category", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @ModelAttribute CreateCategoryRequest request) throws IOException {
        var response = productService.createCategory(request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<CategoryByIdResponse>> getCategoryById(
            @PathVariable Long id) {
        var response = productService.getCategoryById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryByIdResponse>>> getAllCategories() {
        var response = productService.getAllCategories();
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PatchMapping(value = "/category/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UpdateCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @ModelAttribute UpdateCategoryRequest request) throws IOException {
        var response = productService.updateCategory(id, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @DeleteMapping("category/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        productService.deleteCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @Operation(
            summary = "Məhsul yarat",
            description = "multipart/form-data: **product** — JSON mətn (Postman çox vaxt application/octet-stream göndərir; server parse edir); "
                    + "**images** — eyni adda bir neçə fayl (optional)."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @RequestPart("product") CreateProductRequest productRequest,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart("variant") ProductVariantRequest variantRequest) throws IOException {
        var response = productService.createProduct(productRequest, images, variantRequest);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
