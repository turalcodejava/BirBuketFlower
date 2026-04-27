package com.birbuket.productservice.specification;

import com.birbuket.productservice.models.Product;
import com.birbuket.productservice.models.ProductCategory;
import com.birbuket.productservice.models.ProductVariant;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    /**
     * @param categoryId   API-dan gələn kateqoriya {@code id} (məs. dropdown). Veriləndə {@code categoryTitle}dan üstün tutulur.
     * @param categoryTitle  Köhnə davranış: kateqoriya adı ilə tam uyğunluq (trim). {@code categoryId} verilənədək istifadə olunur.
     */
    public static Specification<Product> filter(
            Long categoryId,
            String categoryTitle,
            BigDecimal minPrice,
            BigDecimal maxPrice
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            Join<Product, ProductVariant> variantJoin = null;

            if (query != null) {
                query.distinct(true);
            }

            if (categoryId != null) {
                Join<Product, ProductCategory> categoryJoin = root.join("productCategory");
                predicates.add(cb.equal(categoryJoin.get("id"), categoryId));
            } else if (categoryTitle != null && !categoryTitle.isBlank()) {
                Join<Product, ProductCategory> categoryJoin = root.join("productCategory");
                predicates.add(cb.equal(categoryJoin.get("title"), categoryTitle.trim()));
            }

            if (minPrice != null) {
                if (variantJoin == null) {
                    variantJoin = root.join("productVariants");
                }
                predicates.add(cb.greaterThanOrEqualTo(variantJoin.get("price"), minPrice));
            }

            if (maxPrice != null) {
                if (variantJoin == null) {
                    variantJoin = root.join("productVariants");
                }
                predicates.add(cb.lessThanOrEqualTo(variantJoin.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
