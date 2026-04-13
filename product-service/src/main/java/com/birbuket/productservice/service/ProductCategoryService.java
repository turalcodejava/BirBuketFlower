package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.category.*;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

public interface ProductCategoryService {

    @Transactional
    CreateCategoryResponse createCategory(CreateCategoryRequest request) throws IOException;

    @Transactional(readOnly = true)
    CategoryByIdResponse getCategoryById(Long id);

    @Transactional(readOnly = true)
    List<CategoryByIdResponse> getAllCategories();

    @Transactional
    UpdateCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) throws IOException;

    @Transactional
    void deleteCategoryById(Long id);
}
