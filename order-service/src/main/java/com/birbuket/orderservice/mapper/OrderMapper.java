package com.birbuket.orderservice.mapper;

import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.models.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {
    public CreateOrderResponse toOrderResponse(Order savedOrder) {
        CreateOrderResponse response = new CreateOrderResponse();
        response.setOrderId(savedOrder.getId());
        response.setUserId(savedOrder.getUserId() == null ? null : Long.valueOf(savedOrder.getUserId()));
        response.setTotalPrice(savedOrder.getTotalPrice());
        response.setStatus(savedOrder.getStatus());
        response.setDiscountPrice(savedOrder.getDiscountPrice());
        response.setItems(savedOrder.getItems());
        response.setDeliveryFee(savedOrder.getDeliveryFee());
        response.setAddressLine(savedOrder.getAddressLine());
        response.setCity(savedOrder.getCity());
        response.setAddressNote(savedOrder.getAddressNote());
        response.setDeliveryDate(savedOrder.getDeliveryDate());
        response.setDeliveryTimeSlot(savedOrder.getDeliveryTimeSlot());
        response.setPaymentMethod(savedOrder.getPaymentMethod());
        response.setPaymentUrl(savedOrder.getPaymentUrl());
        response.setPaymentReference(savedOrder.getPaymentReference());
        response.setPaidAt(savedOrder.getPaidAt());
        response.setCreatedAt(savedOrder.getCreatedAt());
        return response;
    }
}
