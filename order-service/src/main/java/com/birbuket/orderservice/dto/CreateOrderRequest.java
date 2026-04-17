package com.birbuket.orderservice.dto;

import com.birbuket.orderservice.models.OrderItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class CreateOrderRequest {

    Long userId;

    @NotNull(message = "Total price boş ola bilməz")
    @PositiveOrZero(message = "Total price mənfi ola bilməz")
    BigDecimal totalPrice;

    @PositiveOrZero(message = "Discount price mənfi ola bilməz")
    BigDecimal discountPrice;

    @PositiveOrZero(message = "Delivery fee mənfi ola bilməz")
    BigDecimal deliveryFee;

    List<OrderItemRequest> orderItems;
}
