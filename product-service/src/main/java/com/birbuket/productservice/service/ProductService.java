package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.exception.CategoryAlreadyExistsException;
import com.birbuket.productservice.exception.CategoryNotFoundException;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.repository.CategoryRepository;
import jdk.jfr.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;

    public CreateCategoryResponse createCategory(CreateCategoryRequest createCategoryRequest) throws IOException {
        if (categoryRepository.existsByTitle(createCategoryRequest.getTitle())) {
            throw new CategoryAlreadyExistsException("Category already exists with title" + createCategoryRequest.getTitle());
        }
        String imageUrl = fileUploadService.uploadFile(createCategoryRequest.getImage(), "categories");

        var category = ProductCategory.builder()
                .title(createCategoryRequest.getTitle())
                .subtitle(createCategoryRequest.getSubtitle())
                .imageUrl(imageUrl)
                .build();

        return categoryMapper.toCreateCategoryResponse(categoryRepository.save(category));
    }

    public CategoryByIdResponse getCategoryById(Long id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(
                        () -> new CategoryNotFoundException("Category not found with id " + id));
        return categoryMapper.toCategoryById(category);
    }

    public List<CategoryByIdResponse> getAllCategories() {
        var categories = categoryRepository.findAll();
        return categoryMapper.toAllCategory(categories);
    }

    public UpdateCategoryResponse updateCategory(Long id, UpdateCategoryRequest request) throws IOException {
        ProductCategory category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category not found with id " + id)
        );

        category.setTitle(request.getTitle());
        category.setSubtitle(request.getSubtitle());
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            String imageUrl = fileUploadService.uploadFile(request.getImage(), "categories");
            category.setImageUrl(imageUrl);
        }
        var saved = categoryRepository.save(category);
        return categoryMapper.toUpdateCategoryResponse(saved);
    }
}
