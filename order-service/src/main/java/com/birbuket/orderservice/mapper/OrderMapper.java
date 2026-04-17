package com.birbuket.orderservice.mapper;

import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.models.Order;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    CreateOrderResponse toOrderResponse(Order savedOrder);
}
