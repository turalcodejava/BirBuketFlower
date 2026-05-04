package com.birbuket.orderservice.controller;

import com.birbuket.orderservice.dto.AddCartItemRequest;
import com.birbuket.orderservice.dto.CartResponse;
import com.birbuket.orderservice.dto.CreateCartRequest;
import com.birbuket.orderservice.dto.UpdateCartItemRequest;
import com.birbuket.orderservice.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping
    public ResponseEntity<CartResponse> createCart(
            @RequestParam(required = false) Long userId,
            @Valid @RequestBody(required = false) CreateCartRequest request) {
        if (userId == null && request != null) {
            userId = request.getUserId();
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        return ResponseEntity.ok(cartService.createCart(userId));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CartResponse> getCart(@PathVariable Long userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/{userId}/items")
    public ResponseEntity<CartResponse> addItem(
            @PathVariable Long userId,
            @Valid @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request));
    }

    @PutMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> updateItemQuantity(
            @PathVariable Long userId,
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, productId, request));
    }

    @DeleteMapping("/{userId}/items/{productId}")
    public ResponseEntity<CartResponse> removeItem(
            @PathVariable Long userId,
            @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItem(userId, productId));
    }

    @DeleteMapping("/{userId}/items")
    public ResponseEntity<Void> clearCart(@PathVariable Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }
}
