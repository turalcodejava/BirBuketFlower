package com.birbuket.orderservice.client;

import com.birbuket.orderservice.dto.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/v1/variants/{id}")
    ProductVariantResponse getVariant(@PathVariable Long id);
}
