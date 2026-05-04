package com.birbuket.orderservice.dto;

import com.birbuket.orderservice.enums.DeliveryTimeSlot;
import com.birbuket.orderservice.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CheckoutRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "addressLine is required")
    private String addressLine;

    @NotBlank(message = "city is required")
    private String city;

    private String addressNote;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;

    @NotNull(message = "deliveryDate is required")
    private LocalDate deliveryDate;

    @NotNull(message = "deliveryTimeSlot is required")
    private DeliveryTimeSlot deliveryTimeSlot;
}
