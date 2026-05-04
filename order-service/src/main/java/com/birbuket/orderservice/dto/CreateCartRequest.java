package com.birbuket.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateCartRequest {
    @NotNull
    private Long userId;
}
