package com.birbuket.orderservice.service;

import com.birbuket.orderservice.dto.CreateOrderRequest;
import com.birbuket.orderservice.dto.CreateOrderResponse;
import org.springframework.security.core.Authentication;

public interface OrderService {

    CreateOrderResponse createOrder(CreateOrderRequest request,
                                    Authentication authentication);
}
