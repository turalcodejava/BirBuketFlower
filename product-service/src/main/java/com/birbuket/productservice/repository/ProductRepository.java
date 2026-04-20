package com.birbuket.productservice.repository;

import aj.org.objectweb.asm.commons.Remapper;
import com.birbuket.productservice.models.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsByProductName(String productName);

    Optional<Product> findBySlug(String slug);

    Page<Product> findByProductCategory_Id(Long categoryId, Pageable pageable);
}
