package com.birbuket.orderservice.models;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.birbuket.orderservice.enums.OrderStatus;
import com.birbuket.orderservice.enums.DeliveryTimeSlot;
import com.birbuket.orderservice.enums.PaymentMethod;
import org.hibernate.annotations.ColumnDefault;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    @JsonManagedReference
    List<OrderItem> items = new ArrayList<>();

    @PositiveOrZero(message = "Delivery fee mənfi ola bilməz")
    @Column(name = "delivery_fee", precision = 10, scale = 2)
    BigDecimal deliveryFee;

    @Column(name = "address_line", nullable = false, length = 300)
    String addressLine;

    @Column(name = "city", nullable = false, length = 120)
    String city;

    @Column(name = "address_note", length = 500)
    String addressNote;

    @Column(name = "delivery_date", nullable = false)
    @ColumnDefault("CURRENT_DATE")
    LocalDate deliveryDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_time_slot", nullable = false, length = 20)
    @ColumnDefault("'SLOT_09_12'")
    DeliveryTimeSlot deliveryTimeSlot;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false, length = 20)
    PaymentMethod paymentMethod;

    @Column(name = "payment_url", length = 1000)
    String paymentUrl;

    @Column(name = "payment_reference", length = 100)
    String paymentReference;

    @Column(name = "paid_at")
    LocalDateTime paidAt;

    LocalDateTime createdAt;


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.status = OrderStatus.CREATED;
    }


}
