package com.birbuket.productservice.service.impl;

import com.birbuket.productservice.dto.product.variants.CreateVariantsRequest;
import com.birbuket.productservice.dto.product.variants.CreateVariantsResponse;
import com.birbuket.productservice.dto.product.variants.VariantSearchById;
import com.birbuket.productservice.exception.ProductNotFoundException;
import com.birbuket.productservice.exception.VariantsNotFoundException;
import com.birbuket.productservice.mapper.ProductMapper;
import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductVariant;
import com.birbuket.productservice.repository.ProductRepository;
import com.birbuket.productservice.repository.ProductVariantRepository;
import com.birbuket.productservice.service.ProductVariantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class ProductVariantServiceImpl implements ProductVariantService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductMapper productMapper;

    @Transactional
    @Override
    public CreateVariantsResponse createVariants(Long id, CreateVariantsRequest request) {
        Product product = productRepository.findById(id).orElseThrow(
                () -> new ProductNotFoundException("Product not found with id " + id));

        ProductVariant variant = ProductVariant.builder()
                .color(request.getColor())
                .size(request.getSize())
                .price(request.getPrice())
                .product(product)
                .build();
        product.getProductVariants().add(variant);

        var savedVariant = productVariantRepository.save(variant);

        return productMapper.toCreateVariantsResponse(savedVariant);
    }

    @Transactional(readOnly = true)
    @Override
    public VariantSearchById getVariantById(Long id){
        var variant = productVariantRepository.findById(id).orElseThrow(
                ()-> new VariantsNotFoundException("Variants  not found with id " + id)
        );
        return productMapper.toVariantSeachById(variant);
    }
}
