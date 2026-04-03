package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.exception.CategoryNotFoundException;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @PostMapping(value = "/category", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @ModelAttribute CreateCategoryRequest request) throws IOException {
        var response = productService.createCategory(request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category/{id}")
    public ResponseEntity<ApiResponse<CategoryByIdResponse>> getCategoryById(
            @PathVariable Long id){
        var response = productService.getCategoryById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/category")
    public ResponseEntity<ApiResponse<List<CategoryByIdResponse>>> getAllCategories(){
        var response =  productService.getAllCategories();
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PatchMapping(value = "/category/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UpdateCategoryResponse>> updateCategory(
            @PathVariable Long id,
            @ModelAttribute UpdateCategoryRequest request) throws IOException {
        var response = productService.updateCategory(id,request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
