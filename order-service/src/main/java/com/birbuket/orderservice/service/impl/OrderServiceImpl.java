package com.birbuket.orderservice.service.impl;

import com.birbuket.orderservice.client.ProductClient;
import com.birbuket.orderservice.dto.CheckoutRequest;
import com.birbuket.orderservice.dto.CreateOrderRequest;
import com.birbuket.orderservice.dto.CreateOrderResponse;
import com.birbuket.orderservice.dto.OrderItemRequest;
import com.birbuket.orderservice.dto.PayOrderRequest;
import com.birbuket.orderservice.enums.DeliveryTimeSlot;
import com.birbuket.orderservice.enums.OrderStatus;
import com.birbuket.orderservice.enums.PaymentMethod;
import com.birbuket.orderservice.mapper.OrderMapper;
import com.birbuket.orderservice.models.CartItem;
import com.birbuket.orderservice.models.Order;
import com.birbuket.orderservice.models.OrderItem;
import com.birbuket.orderservice.payment.EpointPaymentService;
import com.birbuket.orderservice.repository.CartRepository;
import com.birbuket.orderservice.repository.OrderRepository;
import com.birbuket.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final OrderMapper orderMapper;
    private final EpointPaymentService epointPaymentService;
    @Value("${order.delivery.local-city-fee:3.00}")
    private BigDecimal localCityFee;
    @Value("${order.delivery.regional-city-fee:6.00}")
    private BigDecimal regionalCityFee;
    @Value("${order.delivery.remote-area-extra-fee:2.00}")
    private BigDecimal remoteAreaExtraFee;
    @Value("${order.delivery.min-days-ahead:1}")
    private int minDaysAhead;
    @Value("${order.delivery.max-days-ahead:7}")
    private int maxDaysAhead;

    @Override
    @Transactional
    public CreateOrderResponse createOrder(CreateOrderRequest request,
                                           Authentication authentication) {
        if (authentication == null) {
            throw new RuntimeException("Username or password is invalid");
        }
        var userId = authentication.getName();
        List<OrderItem> items = new ArrayList<>();
        var total = BigDecimal.ZERO;
        for (OrderItemRequest orderItemRequest : request.getOrderItems()){

            var variant = productClient.getVariant(orderItemRequest.getProductVariantId());
            var price = variant.getPrice();

            var itemTotal = price.multiply(BigDecimal.valueOf(orderItemRequest.getQuantity()));

            total = total.add(itemTotal);

            var item = OrderItem.builder()
                    .productVariantId(orderItemRequest.getProductVariantId())
                    .quantity(orderItemRequest.getQuantity())
                    .price(price)
                    .build();

            items.add(item);
        }

        var order = Order.builder()
                .userId(userId)
                .items(items)
                .totalPrice(total)
                .deliveryFee(request.getDeliveryFee())
                .discountPrice(BigDecimal.ZERO)
                .addressLine("N/A")
                .city("N/A")
                .deliveryDate(LocalDate.now().plusDays(minDaysAhead))
                .deliveryTimeSlot(DeliveryTimeSlot.SLOT_09_12)
                .paymentMethod(PaymentMethod.CASH)
                .build();

        items.forEach(i -> i.setOrder(order));

        var savedOrder = orderRepository.save(order);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public CreateOrderResponse checkout(CheckoutRequest request) {
        var cart = cartRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty");
        }

        List<OrderItem> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            BigDecimal itemTotal = cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(itemTotal);

            OrderItem orderItem = OrderItem.builder()
                    .productVariantId(cartItem.getProductId())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getUnitPrice())
                    .build();
            items.add(orderItem);
        }
        BigDecimal deliveryFee = calculateDeliveryFee(request.getCity(), request.getAddressLine(), request.getAddressNote());
        BigDecimal finalTotal = total.add(deliveryFee);
        validateDeliverySchedule(request.getDeliveryDate());

        Order order = Order.builder()
                .userId(String.valueOf(request.getUserId()))
                .items(items)
                .totalPrice(finalTotal)
                .discountPrice(BigDecimal.ZERO)
                .deliveryFee(deliveryFee)
                .addressLine(request.getAddressLine().trim())
                .city(request.getCity().trim())
                .addressNote(request.getAddressNote())
                .deliveryDate(request.getDeliveryDate())
                .deliveryTimeSlot(request.getDeliveryTimeSlot())
                .paymentMethod(request.getPaymentMethod())
                .status(OrderStatus.PENDING)
                .build();

        items.forEach(i -> i.setOrder(order));
        Order savedOrder = orderRepository.save(order);

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            var payment = epointPaymentService.createPaymentRequest(savedOrder);
            savedOrder.setPaymentUrl(payment.paymentUrl());
            savedOrder.setPaymentReference(payment.paymentReference());
            savedOrder = orderRepository.save(savedOrder);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return orderMapper.toOrderResponse(savedOrder);
    }

    @Override
    @Transactional
    public CreateOrderResponse pay(PayOrderRequest request) {
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Cancelled order cannot be paid");
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        Order saved = orderRepository.save(order);
        return orderMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CreateOrderResponse> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(String.valueOf(userId))
                .stream()
                .map(orderMapper::toOrderResponse)
                .toList();
    }

    private BigDecimal calculateDeliveryFee(String city, String addressLine, String addressNote) {
        String cityNorm = normalize(city);
        String addressNorm = normalize((addressLine == null ? "" : addressLine) + " " + (addressNote == null ? "" : addressNote));

        boolean isBaku = cityNorm.contains("baku") || cityNorm.contains("bak");
        BigDecimal fee = isBaku ? localCityFee : regionalCityFee;

        if (addressNorm.contains("qesebe")
                || addressNorm.contains("kend")
                || cityNorm.contains("naxc")
                || cityNorm.contains("quba")
                || cityNorm.contains("qebele")
                || cityNorm.contains("zaqatala")
                || cityNorm.contains("lerik")
                || cityNorm.contains("yardimli")) {
            fee = fee.add(remoteAreaExtraFee);
        }
        return fee;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.toLowerCase()
                .replace('ə', 'e')
                .replace('ı', 'i')
                .replace('ö', 'o')
                .replace('ü', 'u')
                .replace('ğ', 'g')
                .replace('ş', 's')
                .replace('ç', 'c')
                .trim();
    }

    private void validateDeliverySchedule(LocalDate deliveryDate) {
        LocalDate today = LocalDate.now();
        LocalDate minDate = today.plusDays(minDaysAhead);
        LocalDate maxDate = today.plusDays(maxDaysAhead);

        if (deliveryDate.isBefore(minDate) || deliveryDate.isAfter(maxDate)) {
            throw new IllegalArgumentException(
                    "Delivery date must be between " + minDate + " and " + maxDate);
        }
    }
}
