package com.birbuket.productservice.service.impl;

import com.birbuket.productservice.dto.product.*;
import com.birbuket.productservice.exception.*;
import com.birbuket.productservice.mapper.CategoryMapper;
import com.birbuket.productservice.mapper.ProductMapper;
import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.models.ProductImage;
import com.birbuket.productservice.models.ProductVariant;
import com.birbuket.productservice.repository.CategoryRepository;
import com.birbuket.productservice.repository.ProductImageRepository;
import com.birbuket.productservice.repository.ProductRepository;
import com.birbuket.productservice.repository.ProductVariantRepository;
import com.birbuket.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final ProductMapper productMapper;
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;




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
                .slug(request.getSlug())
                .sku(request.getSku())
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
        List<String> imageUrls = fileUploadService.uploadMultipartFiles(imageFiles, "product_image");

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


    @Transactional(readOnly = true)
    @Override
    public ProductByIdResponse getProductById(Long id) {
        var product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id " + id));
        return productMapper.toProductByIdResponse(product);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductByIdResponse> getViewAllProducts() {
        var products = productRepository.findAll();
        return productMapper.toALlProduct(products);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id){
        var product = productRepository.findById(id).orElseThrow(
                ()-> new ProductNotFoundException("Product not found with id " + id)
        );
        productRepository.delete(product);
    }

    @Transactional
    @Override
    public UpdateProductResponse updateProductResponse(Long id, UpdateProductRequest request){
        var product = productRepository.findById(id).orElseThrow(
                ()-> new ProductNotFoundException("Product not found with id" + id)
        );

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setComposition(request.getComposition());
        product.setSlug(request.getSlug());
        product.setSku(request.getSku());
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
}
