package com.birbuket.orderservice.repository;

import com.birbuket.orderservice.models.Cart;
import com.birbuket.orderservice.models.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    Optional<CartItem> findByCartAndProductId(Cart cart, Long productId);
}
