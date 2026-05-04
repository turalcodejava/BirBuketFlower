package com.birbuket.orderservice.controller;


import com.birbuket.common.dto.ApiResponse;
import com.birbuket.orderservice.dto.CheckoutRequest;
import com.birbuket.orderservice.dto.CreateOrderRequest;
import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.dto.PayOrderRequest;
import com.birbuket.orderservice.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequest createOrderRequest,
            Authentication authentication){
        var response = orderService.createOrder(createOrderRequest, authentication);
        return ResponseEntity.ok().body(ApiResponse.success(response));
    }

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.checkout(request)));
    }

    @PostMapping("/pay")
    public ResponseEntity<ApiResponse<CreateOrderResponse>> pay(@Valid @RequestBody PayOrderRequest request) {
        return ResponseEntity.ok(ApiResponse.success(orderService.pay(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CreateOrderResponse>>> getOrdersByUser(@RequestParam Long userId) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrdersByUser(userId)));
    }
}
