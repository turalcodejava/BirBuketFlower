package com.birbuket.productservice.service.impl;

import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.exception.CategoryAlreadyExistsException;
import com.birbuket.productservice.exception.CategoryNotFoundException;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImpl implements ProductCategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    @Value("${app.upload.category-folder:categories}")
    private String categoryImageFolder;

    @Transactional
    @Override
    public CreateCategoryResponse createCategory(CreateCategoryRequest request) throws IOException {
        if (categoryRepository.existsByTitle(request.getTitle())) {
            throw new CategoryAlreadyExistsException("Category already exists with title" + request.getTitle());
        }
        String imageUrl = null;
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            imageUrl = fileUploadService.uploadFile(request.getImage(), categoryImageFolder);
        }

        var category = ProductCategory.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .imageUrl(imageUrl)
                .build();

        return categoryMapper.toCreateCategoryResponse(categoryRepository.save(category));
    }

    @Transactional(readOnly = true)
    @Override
    public CategoryByIdResponse getCategoryById(Long id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException("Category not found with id " + id));
        return categoryMapper.toCategoryById(category);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CategoryByIdResponse> getAllCategories() {
        var categories = categoryRepository.findAll();
        return categoryMapper.toAllCategory(categories);
    }


    @Transactional
    @Override
    public UpdateCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) throws IOException {
        ProductCategory category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category not found with id " + id)
        );

        category.setTitle(request.getTitle());
        category.setSubtitle(request.getSubtitle());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileUploadService.uploadFile(request.getImage(), categoryImageFolder);
            category.setImageUrl(imageUrl);
        }
        var saved = categoryRepository.save(category);
        return categoryMapper.toUpdateCategoryResponse(saved);
    }

    @Transactional
    @Override
    public void deleteCategoryById(Long id) {
        var category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category not found with id " + id));
        categoryRepository.delete(category);
    }
}
