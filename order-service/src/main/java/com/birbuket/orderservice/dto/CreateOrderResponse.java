package com.birbuket.orderservice.dto;


import com.birbuket.orderservice.enums.OrderStatus;
import com.birbuket.orderservice.models.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderResponse {

    Long userId;
    BigDecimal totalPrice;
    OrderStatus status;
    BigDecimal discountPrice;
    List<OrderItem> items;
    BigDecimal deliveryFee;
    LocalDateTime createdAt;
}
