package com.birbuket.orderservice.dto;


import com.birbuket.orderservice.enums.OrderStatus;
import com.birbuket.orderservice.enums.DeliveryTimeSlot;
import com.birbuket.orderservice.enums.PaymentMethod;
import com.birbuket.orderservice.models.OrderItem;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class CreateOrderResponse {

    Long orderId;
    Long userId;
    BigDecimal totalPrice;
    OrderStatus status;
    BigDecimal discountPrice;
    List<OrderItem> items;
    BigDecimal deliveryFee;
    String addressLine;
    String city;
    String addressNote;
    LocalDate deliveryDate;
    DeliveryTimeSlot deliveryTimeSlot;
    PaymentMethod paymentMethod;
    String paymentUrl;
    String paymentReference;
    LocalDateTime paidAt;
    LocalDateTime createdAt;
}
