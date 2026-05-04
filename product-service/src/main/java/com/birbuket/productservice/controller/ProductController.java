package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.product.*;
import com.birbuket.productservice.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
@Validated
public class ProductController {

    private final ProductService productService;
    private final ObjectMapper objectMapper;
    private final Validator validator;


    @Operation(summary = "Məhsul yarat")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateProductResponse>> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false)
            List<MultipartFile> images) throws IOException {
        CreateProductRequest request = resolveCreateProductRequest(productJson);
        var response = productService.createProduct(request, images);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    private CreateProductRequest resolveCreateProductRequest(String productJson) throws JsonProcessingException {
        CreateProductRequest request = objectMapper.readValue(productJson, CreateProductRequest.class);

        Set<ConstraintViolation<CreateProductRequest>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        return request;
    }

    @GetMapping({"/{slug}"})
    @Operation(summary = "Slug-a gore product axtar")
    public ResponseEntity<ApiResponse<ProductBySlugResponse>> getProductBySlug(
            @PathVariable String slug) {
        var response = productService.getProductBySlug(slug);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(
            summary = "Productlar-in siyahisina bax",
            description = "Birbuket yarat / tək çiçək seçimi üçün məs: isSingle=true — yalnız is_single=true məhsullar")
    public ResponseEntity<ApiResponse<Page<ProductBySlugResponse>>> getAllProducts(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) Boolean isSingle) {
        var response = productService.getViewAllProducts(size, page, isSingle);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Id-ye gore product sil")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().body(ApiResponse.success(null));
    }

    @PatchMapping("{id}")
    @Operation(summary = "Id-ye gore product update etmek")
    public ResponseEntity<ApiResponse<UpdateProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProductRequest request) {
        var response = productService.updateProductResponse(id, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category/{id}/product")
    @Operation(summary = "Product-ra category-ye gore baxmaq")
    public ResponseEntity<ApiResponse<Page<ProductByCategoryResponse>>> getAllProductsByCategory(
            @PathVariable String id,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) Boolean isSingle) {
        var response = productService.getProductByCategory(id, size, page, isSingle);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<List<ProductBySlugResponse>>> filterProduct(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Boolean isSingle
    ) {
        var response = productService.filterProducts(categoryId, category, minPrice, maxPrice, isSingle);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
