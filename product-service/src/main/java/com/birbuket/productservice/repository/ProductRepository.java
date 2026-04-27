package com.birbuket.productservice.repository;

import com.birbuket.productservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    boolean existsByProductName(String productName);

    Optional<Product> findBySlug(String slug);
    Optional<Product> findFirstBySlugStartingWith(String slugPrefix);

    Page<Product> findByProductCategory_Id(Long categoryId, Pageable pageable);
}
