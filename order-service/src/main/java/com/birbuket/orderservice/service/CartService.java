package com.birbuket.orderservice.service;

import com.birbuket.orderservice.dto.AddCartItemRequest;
import com.birbuket.orderservice.dto.CartItemResponse;
import com.birbuket.orderservice.dto.CartResponse;
import com.birbuket.orderservice.dto.UpdateCartItemRequest;
import com.birbuket.orderservice.models.Cart;
import com.birbuket.orderservice.models.CartItem;
import com.birbuket.orderservice.repository.CartItemRepository;
import com.birbuket.orderservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    @Transactional
    public CartResponse createCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
        return toResponse(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> Cart.builder().userId(userId).items(new ArrayList<>()).build());
        return toResponse(cart);
    }

    @Transactional
    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId).orElseGet(() -> cartRepository.save(Cart.builder().userId(userId).build()));
        CartItem item = cartItemRepository.findByCartAndProductId(cart, request.getProductId())
                .map(existing -> {
                    existing.setQuantity(existing.getQuantity() + request.getQuantity());
                    existing.setUnitPrice(request.getUnitPrice());
                    existing.setProductName(request.getProductName());
                    return existing;
                })
                .orElseGet(() -> CartItem.builder()
                        .cart(cart)
                        .productId(request.getProductId())
                        .productName(request.getProductName())
                        .unitPrice(request.getUnitPrice())
                        .quantity(request.getQuantity())
                        .build());

        cartItemRepository.save(item);
        return toResponse(cartRepository.findByUserId(userId).orElse(cart));
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId, Long productId, UpdateCartItemRequest request) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found for product: " + productId));

        item.setQuantity(request.getQuantity());
        cartItemRepository.save(item);

        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartAndProductId(cart, productId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found for product: " + productId));

        cartItemRepository.delete(item);
        cart.getItems().remove(item);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(Long userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Cart not found for user: " + userId));
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream()
                .map(item -> {
                    BigDecimal lineTotal = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                    return CartItemResponse.builder()
                            .productId(item.getProductId())
                            .productName(item.getProductName())
                            .unitPrice(item.getUnitPrice())
                            .quantity(item.getQuantity())
                            .lineTotal(lineTotal)
                            .build();
                })
                .toList();

        BigDecimal total = items.stream()
                .map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .userId(cart.getUserId())
                .items(items)
                .totalAmount(total)
                .build();
    }
}
