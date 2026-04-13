package com.birbuket.productservice.service;

import com.birbuket.productservice.dto.product.variants.CreateVariantsRequest;
import com.birbuket.productservice.dto.product.variants.CreateVariantsResponse;
import com.birbuket.productservice.dto.product.variants.VariantSearchById;
import org.springframework.transaction.annotation.Transactional;

public interface ProductVariantService {
    CreateVariantsResponse createVariants(Long productId, CreateVariantsRequest request);

    VariantSearchById getVariantById(Long id);

}
