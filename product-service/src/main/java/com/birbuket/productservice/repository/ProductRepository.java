package com.birbuket.productservice.repository;

import com.birbuket.productservice.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductName(String productName);
}
