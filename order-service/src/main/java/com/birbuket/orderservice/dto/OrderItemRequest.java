package com.birbuket.orderservice.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class OrderItemRequest {


    @NotNull(message = "Product variant id boş ola bilməz")
    private Long productVariantId;

    @NotNull(message = "Quantity boş ola bilməz")
    @Min(value = 1, message = "Quantity ən azı 1 olmalıdır")
    private Integer quantity;

}
