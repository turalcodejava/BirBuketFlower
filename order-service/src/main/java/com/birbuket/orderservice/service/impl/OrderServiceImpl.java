package com.birbuket.orderservice.service.impl;

import com.birbuket.orderservice.client.ProductClient;
import com.birbuket.orderservice.dto.CreateOrderRequest;
import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.dto.OrderItemRequest;
import com.birbuket.orderservice.mapper.OrderMapper;
import com.birbuket.orderservice.models.Order;
import com.birbuket.orderservice.models.OrderItem;
import com.birbuket.orderservice.repository.OrderRepository;
import com.birbuket.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;

    @Override
    public CreateOrderResponse createOrder(CreateOrderRequest request,
                                           Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Username or password is invalid");
        }
        var userId = authentication.getName();
        List<OrderItem> items = new ArrayList<>();
        var total = BigDecimal.ZERO;
        for (OrderItemRequest orderItemRequest : request.getOrderItems()){

            var variant = productClient.getVariant(orderItemRequest.getProductVariantId());
            var price = variant.getPrice();

            var itemTotal = price.multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));

            total = total.add(itemTotal);

            var item = OrderItem.builder()
                    .productVariantId(orderItemRequest.getProductVariantId())
                    .quantity(orderItemRequest.getQuantity())
                    .price(price)
                    .build();

            items.add(item);
        }

        var order = Order.builder()
                .userId(userId)
                .items(items)
                .totalPrice(total)
                .deliveryFee(request.getDeliveryFee())
                .discountPrice(BigDecimal.ZERO)
                .build();

        items.forEach(i -> i.setOrder(order));

        var savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(savedOrder);
    }
}
