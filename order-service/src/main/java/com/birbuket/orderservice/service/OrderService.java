package com.birbuket.orderservice.service;

import com.birbuket.orderservice.dto.CheckoutRequest;
import com.birbuket.orderservice.dto.CreateOrderRequest;
import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.dto.PayOrderRequest;
import org.springframework.security.core.Authentication;

import java.util.List;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request,
                                    Authentication authentication);

    CreateOrderResponse checkout(CheckoutRequest request);

    CreateOrderResponse pay(PayOrderRequest request);

    List<CreateOrderResponse> getOrdersByUser(Long userId);
}
