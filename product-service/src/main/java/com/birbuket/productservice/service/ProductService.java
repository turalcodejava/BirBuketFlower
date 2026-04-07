package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.category.*;
import com.birbuket.productservice.dto.product.CreateProductRequest;
import com.birbuket.productservice.dto.product.CreateProductResponse;
import com.birbuket.productservice.exception.CategoryAlreadyExistsException;
import com.birbuket.productservice.exception.CategoryNotFoundException;
import com.birbuket.productservice.exception.ProductNameAlreadyExistsException;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.mapper.ProductMapper;
import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.models.ProductImage;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.repository.ProductRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;


    @Transactional
    public CreateCategoryResponse createCategory(CreateCategoryRequest request) throws IOException {
        if (categoryRepository.existsByTitle(request.getTitle())) {
            throw new CategoryAlreadyExistsException("Category already exists with title" + request.getTitle());
        }
        String imageUrl = fileUploadService.uploadFile(request.getImage(), "categories");

        var category = ProductCategory.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
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


    @Transactional
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

    public void deleteCategoryById(Long id) {
        var category = categoryRepository.findById(id).orElseThrow(
                () -> new CategoryNotFoundException("Category not found with id " + id));
        categoryRepository.delete(category);
    }

    @Transactional
    public CreateProductResponse createProduct(CreateProductRequest request) throws IOException {

        if (productRepository.existsByProductName(request.getProductName())) {
            throw new ProductNameAlreadyExistsException(
                    "Product already exists with name " + request.getProductName());
        }

        var product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .composition(request.getComposition())
                .discountPercentage(request.getDiscountPercentage())
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .slug(request.getSlug())
                .size(request.getSize())
                .sku(request.getSku())
                .build();

        ProductCategory category = categoryRepository.findById(request.getProductCategoryId())
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id " + request.getProductCategoryId()));

        product.setProductCategory(category);

        List<String> imageUrls = fileUploadService.uploadMultipartFiles(request.getImages(), "product_image");

        List<ProductImage> productImages = imageUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .build())
                .toList();

        product.setImages(productImages);

        var savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);
    }
}
