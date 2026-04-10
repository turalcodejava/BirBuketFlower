package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.dto.product.*;
import com.birbuket.productservice.dto.product.variants.CreateVariantsRequest;
import com.birbuket.productservice.dto.product.variants.CreateVariantsResponse;
import com.birbuket.productservice.dto.product.variants.VariantSearchById;
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
    private final ObjectMapper objectMapper;
    private final Validator validator;

    @PostMapping(value = "/category", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Category yarat")
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @ModelAttribute CreateCategoryRequest request) throws IOException {
        var response = productService.createCategory(request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category/{id}")
    @Operation(summary = "Id-ye gore category axtar")
    public ResponseEntity<ApiResponse<CategoryByIdResponse>> getCategoryById(
            @PathVariable Long id) {
        var response = productService.getCategoryById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category")
    @Operation(summary = "Category-rin hamisina bax")
    public ResponseEntity<ApiResponse<List<CategoryByIdResponse>>> getAllCategories() {
        var response = productService.getAllCategories();
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PatchMapping(value = "/category/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Id-ye gore category update")
    public ResponseEntity<ApiResponse<UpdateCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @ModelAttribute UpdateCategoryRequest request) throws IOException {
        var response = productService.updateCategory(id, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @DeleteMapping("category/{id}")
    @Operation(summary = "Id-ye gore category sil")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        productService.deleteCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

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

    @GetMapping({"{id}"})
    @Operation(summary = "Id-ye gore product axtar")
    public ResponseEntity<ApiResponse<ProductByIdResponse>> getProductById(
            @PathVariable Long id) {
        var response = productService.getProductById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "Productlar-in siyahisina bax")
    public ResponseEntity<ApiResponse<List<ProductByIdResponse>>> getAllProducts() {
        var response = productService.getViewAllProducts();
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

    /**
     * Məhsul ID-si URL-də: {@code POST /api/product/{productId}/variants}
     */
    @PostMapping("/{productId}/variants")
    @Operation(summary = "Məhsula ayrıca variant əlavə et (product id path-də)")
    public ResponseEntity<ApiResponse<CreateVariantsResponse>> createVariantForProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantsRequest request) {
        var response = productService.createVariants(productId, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PostMapping("/variant/{id}")
    @Operation(summary = "Yeni variant (köhnə path: product id = {id})")
    public ResponseEntity<ApiResponse<CreateVariantsResponse>> createVariant(
            @PathVariable Long id,
            @Valid @RequestBody CreateVariantsRequest request) {
        var response = productService.createVariants(id, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

   @GetMapping("/variant/{id}")
   @Operation(summary = "Id-ye gore variant axtarisi")
   public ResponseEntity<ApiResponse<VariantSearchById>> getVariantById(
           @PathVariable Long id){
        var response = productService.getVariantById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
   }
}
