package com.birbuket.productservice.repository;

import com.birbuket.productservice.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<ProductCategory,Long> {
    boolean existsByTitle(String title);
    boolean existsById(Long id);

    List<ProductCategory> findByTitle(String title);

    Optional<ProductCategory> findFirstByTitleIgnoreCase(String title);
}
