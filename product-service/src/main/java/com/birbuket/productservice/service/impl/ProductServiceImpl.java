package com.birbuket.productservice.service.impl;

import com.birbuket.productservice.dto.product.*;
import com.birbuket.productservice.exception.*;
import com.birbuket.productservice.mapper.ProductMapper;
import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.models.ProductImage;
import com.birbuket.productservice.models.ProductVariant;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.repository.ProductRepository;
import com.birbuket.productservice.service.ProductService;
import com.birbuket.productservice.specification.ProductSpecification;
import com.birbuket.productservice.util.SkuGenerator;
import com.birbuket.productservice.util.SlugGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    @Value("${app.upload.product-folder:product_image}")
    private String productImageFolder;


    @Transactional
    @Override
    public CreateProductResponse createProduct(CreateProductRequest request, List<MultipartFile> images) throws IOException {

        if (productRepository.existsByProductName(request.getProductName())) {
            throw new ProductNameAlreadyExistsException(
                    "Product already exists with name " + request.getProductName());
        }

        var product = Product.builder()
                .productName(request.getProductName())
                .description(request.getDescription())
                .composition(request.getComposition())
                .discountPercentage(request.getDiscountPercentage())
                .active(request.isActive())
                .isSingle(request.isSingle())
                .rating(request.getRating())
                .reviewCount(request.getReviewCount())
                .build();

        Long categoryId = request.getProductCategoryId();
        if (categoryId == null) {
            throw new IllegalArgumentException("Product category id must not be null");
        }

        ProductCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(
                        "Category not found with id " + categoryId));

        product.setProductCategory(category);

        if (request.getProductVariants() != null && !request.getProductVariants().isEmpty()) {
            List<ProductVariant> variants = request.getProductVariants().stream()
                    .map(variantRequest -> ProductVariant.builder()
                            .price(variantRequest.getPrice())
                            .size(variantRequest.getSize())
                            .color(variantRequest.getColor())
                            .product(product)
                            .build())
                    .toList();
            product.setProductVariants(new ArrayList<>(variants));
        }

        List<MultipartFile> imageFiles = images != null ? images : Collections.emptyList();
        List<String> imageUrls = fileUploadService.uploadMultipartFiles(imageFiles, productImageFolder);

        List<ProductImage> productImages = imageUrls.stream()
                .map(url -> ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .build())
                .toList();

        product.setImages(productImages);

        product.setSlug(SlugGenerator.generatorSlug(request.getProductName()));
        product.setSku(SkuGenerator.generatorSku());
        var savedProduct = productRepository.save(product);

        return productMapper.toProductResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    @Override
    public ProductBySlugResponse getProductBySlug(String slug) {
        String normalizedSlug = slug == null ? "" : slug.trim().toLowerCase().replace(" ", "-");
        Product product = productRepository.findBySlug(normalizedSlug)
                .or(() -> productRepository.findFirstBySlugStartingWith(normalizedSlug))
                .orElseThrow(() -> {
                    log.error("Product not found with slug: {}", slug);
                    return new ProductNotFoundException("Product not found with slug " + slug);
                });
        return productMapper.toProductBySlugResponse(product);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductBySlugResponse> getViewAllProducts(int size, int page, Boolean isSingle) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products =
                isSingle == null
                        ? productRepository.findAll(pageable)
                        : productRepository.findByIsSingle(isSingle, pageable);
        return products.map(productMapper::toProductBySlugResponse);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        var product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id " + id)
        );
        productRepository.delete(product);
    }

    @Transactional
    @Override
    public UpdateProductResponse updateProductResponse(Long id, UpdateProductRequest request) {
        var product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id" + id)
        );

        if (request.getProductName() != null) {
            product.setProductName(request.getProductName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getComposition() != null) {
            product.setComposition(request.getComposition());
        }
        if (request.getDiscountPercentage() != null) {
            product.setDiscountPercentage(request.getDiscountPercentage());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getIsSingle() != null) {
            product.setSingle(request.getIsSingle());
        }
        if (request.getRating() != null) {
            product.setRating(request.getRating());
        }
        if (request.getReviewCount() != null) {
            product.setReviewCount(request.getReviewCount());
        }
        if (request.getSlug() != null) {
            product.setSlug(request.getSlug());
        }
        if (request.getSku() != null) {
            product.setSku(request.getSku());
        }
        product.setUpdatedAt(LocalDateTime.now());

        Long categoryId = request.getProductCategoryId();
        if (categoryId != null) {
            ProductCategory category = categoryRepository.findById(categoryId).orElseThrow(
                    () -> new CategoryNotFoundException("Category not found with id " + categoryId));
            product.setProductCategory(category);
        }

        Product updated = productRepository.save(product);
        return productMapper.updateProductResponse(updated);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProductByCategoryResponse> getProductByCategory(
            String categoryKey, int size, int page, Boolean isSingle) {
        Long categoryId = resolveCategoryId(categoryKey);
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> pageResult =
                isSingle == null
                        ? productRepository.findByProductCategory_Id(categoryId, pageable)
                        : productRepository.findByProductCategory_IdAndIsSingle(categoryId, isSingle, pageable);
        return pageResult.map(productMapper::toProductByCategory);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductBySlugResponse> filterProducts(
            Long categoryId,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Boolean isSingle) {
        if (categoryId == null && category != null && !category.isBlank()) {
            categoryId = resolveCategoryId(category);
            category = null;
        }
        Specification<Product> specification =
                ProductSpecification.filter(categoryId, category, minPrice, maxPrice, isSingle);
        return productRepository.findAll(specification)
                .stream()
                .map(productMapper::toProductBySlugResponse)
                .toList();
    }

    private Long resolveCategoryId(String categoryKey) {
        if (categoryKey == null || categoryKey.isBlank()) {
            throw new CategoryNotFoundException("Category key must not be empty");
        }

        String normalizedKey = normalizeCategoryKey(categoryKey);

        try {
            return Long.valueOf(normalizedKey);
        } catch (NumberFormatException ignored) {
            // Continue and resolve by category title/slug.
        }

        String normalizedTitle = normalizedKey.replace("-", " ");

        return categoryRepository.findFirstByTitleIgnoreCase(normalizedTitle)
                .or(() -> categoryRepository.findAll().stream()
                        .filter(category -> normalizeCategoryKey(category.getTitle()).equals(normalizedKey))
                        .findFirst())
                .map(ProductCategory::getId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found with key " + categoryKey));
    }

    private String normalizeCategoryKey(String value) {
        return value.trim()
                .toLowerCase(Locale.ROOT)
                .replace("_", " ")
                .replace("-", " ")
                .replaceAll("\\s+", " ");
    }
}
