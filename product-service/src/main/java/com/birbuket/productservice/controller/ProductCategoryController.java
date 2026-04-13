package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class ProductCategoryController {
    private final ProductCategoryService productCategoryService;

    @PostMapping(value = "/category", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Category yarat")
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @ModelAttribute CreateCategoryRequest request) throws IOException {
        var response = productCategoryService.createCategory(request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category/{id}")
    @Operation(summary = "Id-ye gore category axtar")
    public ResponseEntity<ApiResponse<CategoryByIdResponse>> getCategoryById(
            @PathVariable Long id) {
        var response = productCategoryService.getCategoryById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category")
    @Operation(summary = "Category-rin hamisina bax")
    public ResponseEntity<ApiResponse<List<CategoryByIdResponse>>> getAllCategories() {
        var response = productCategoryService.getAllCategories();
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PatchMapping(value = "/category/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Id-ye gore category update")
    public ResponseEntity<ApiResponse<UpdateCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @ModelAttribute UpdateCategoryRequest request) throws IOException {
        var response = productCategoryService.updateCategory(id, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @DeleteMapping("category/{id}")
    @Operation(summary = "Id-ye gore category sil")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @PathVariable Long id) {
        productCategoryService.deleteCategoryById(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }
}
