package com.birbuket.orderservice.repository;

import com.birbuket.orderservice.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order,Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);
}
