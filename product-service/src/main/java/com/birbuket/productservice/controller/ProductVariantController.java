package com.birbuket.productservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.productservice.dto.product.variants.CreateVariantsRequest;
import com.birbuket.productservice.dto.product.variants.CreateVariantsResponse;
import com.birbuket.productservice.dto.product.variants.VariantSearchById;
import com.birbuket.productservice.service.ProductVariantService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/variant")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantService productVariantService;

    @PostMapping("/{productId}")
    @Operation(summary = "Məhsula ayrıca variant əlavə et (product id path-də)")
    public ResponseEntity<ApiResponse<CreateVariantsResponse>> createVariantForProduct(
            @PathVariable Long productId,
            @Valid @RequestBody CreateVariantsRequest request) {
        var response = productVariantService.createVariants(productId, request);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Id-ye gore variant axtarisi")
    public ResponseEntity<ApiResponse<VariantSearchById>> getVariantById(
            @PathVariable Long id){
        var response = productVariantService.getVariantById(id);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }
}
