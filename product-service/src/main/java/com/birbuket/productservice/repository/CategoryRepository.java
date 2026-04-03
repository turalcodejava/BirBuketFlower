package com.birbuket.productservice.repository;

import com.birbuket.productservice.models.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<ProductCategory,Long> {
    boolean existsByTitle(String title);
    boolean existsById(Long id);

}
