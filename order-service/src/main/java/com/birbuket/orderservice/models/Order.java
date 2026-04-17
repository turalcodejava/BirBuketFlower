package com.birbuket.orderservice.models;

import com.birbuket.orderservice.enums.OrderStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String userId;

    @NotNull(message = "Total price boş ola bilməz")
    @PositiveOrZero(message = "Total price mənfi ola bilməz")
    @Column(name = "total_price", nullable = false, precision = 10, scale = 2)
    BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    OrderStatus status;

    @PositiveOrZero(message = "Discount price mənfi ola bilməz")
    @Column(name = "discount_price", precision = 10, scale = 2)
    BigDecimal discountPrice;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<OrderItem> items = new ArrayList<>();

    @PositiveOrZero(message = "Delivery fee mənfi ola bilməz")
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    BigDecimal deliveryFee;

    LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }


}
