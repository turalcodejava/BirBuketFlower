package com.birbuket.productservice.mapper;

import com.birbuket.productservice.dto.category.CategoryByIdResponse;
import com.birbuket.productservice.dto.category.CreateCategoryRequest;
import com.birbuket.productservice.dto.category.CreateCategoryResponse;
import com.birbuket.productservice.dto.category.UpdateCategoryResponse;
import com.birbuket.productservice.models.ProductCategory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    ProductCategory toEntity(CreateCategoryRequest createCategoryRequest);

    CreateCategoryResponse toCreateCategoryResponse(ProductCategory savedCategory);

    CategoryByIdResponse toCategoryById(ProductCategory byId);

    List<CategoryByIdResponse> toAllCategory(List<ProductCategory> categories);

    UpdateCategoryResponse toUpdateCategoryResponse(ProductCategory category);
}
